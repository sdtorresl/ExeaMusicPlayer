package player;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javafx.scene.media.MediaPlayer;

/**
 *
 * @author sdtorresl
 */
public class FetchStreamBytes implements Runnable {
    private static final int DELAY_TIME = 4000;
    private static final int READ_TIMEOUT = 10000;
    private final String tempFile;
    private final String streamURL;
    private final PlayerController pc;
    private volatile boolean execute;
    private String metadata;

    public FetchStreamBytes (String tempFile, String streamURL, PlayerController pc){
        this.tempFile = tempFile;
        this.streamURL = streamURL;
        this.execute = true;
        this.pc = pc;
        this.metadata = null;
    }
    
    public void stopExecuting(){
        this.execute = false;
    }
    
    public String getMetadata(){
        return this.metadata;
    }
    
    @Override
    public void run() {
        File tF = null;
        FileOutputStream fos = null;
        
        tF = new File (tempFile);
        
        try {
            fos = new FileOutputStream(tF, true);
        } catch (FileNotFoundException ex) {
        }
        
        try
        {
            Counter count = new Counter(tF, fos);
            Thread t;
            int cValue = 0;
            /** Opening connection to the server **/
            HttpURLConnection connection = (HttpURLConnection) new URL(streamURL).openConnection();
            /** Specifying request header so we can get metaint back from server */
            connection.setRequestProperty("Icy-metadata", "1");
            /** Getting metaint out of headers */
            int metaint = Integer.valueOf(connection.getHeaderFields().get("icy-metaint").get(0));
            /** Creating audio buffer with size of our metaint **/
            byte[] audioBuffer = new byte[metaint];
            /** Creating DataInputStream which makes our life easier when comes to read data **/
            DataInputStream stream = new DataInputStream((InputStream) connection.getContent()); 
            count = new Counter(tF, fos);
            t = new Thread (count);
            t.start();
            /** You could specify some other condition here **/
            while (stream != null && execute)
            {
                /** Reading Audio data **/
                stream.readFully(audioBuffer, 0, metaint);
                count.stopExec();
                count = null;
                t = null;
                count = new Counter(tF, fos);
                t = new Thread (count);
                t.start();
                //this.recover(count, tF, fos);
                
                //audioBuffer = readNext (stream, READ_TIMEOUT, metaint);
                
                /** Reading first byte after our audio data **/
                int headerByte = stream.read();
                t = new Thread (count);
                /** We only read metadata if header byte is bigger than 0, if it is 0 this means there is no metadata **/
                if (headerByte > 0)
                {
                    /** Getting our metadata header length **/
                    int headerLength = headerByte * 16;
                    /** Creating byte array which is equal to header size and will store our title **/
                    byte[] titleBuffer = new byte[headerLength];
                    /** Reading title into buffer **/
                    
                    stream.readFully(titleBuffer, 0, headerLength);
                    count.stopExec();
                    count = null;
                    t = null;
                    count = new Counter(tF, fos);
                    t = new Thread (count);
                    t.start();
                    //this.recover(count, tF, fos);
                    
                    //titleBuffer = readNext (stream, READ_TIMEOUT, headerLength);
                    /** Printing out our title **/
                    this.metadata = new String(titleBuffer);
                    //pc.setMetadata();
                }
                fos.write(audioBuffer);
            }
            // following operations are supossed to be done in the main thread
            fos.flush();
            fos.close();
            tF.delete();
        }
        catch (IOException | NumberFormatException e) {
            //Logger.getLogger(FetchStreamBytes.class.getName()).log(Level.SEVERE, null, e);
        } 
    }
    
    public class Counter implements Runnable {
        private static final int STEP = 1;
        private int value;
        private boolean execute;
        File tF;
        FileOutputStream fos;
        
        public Counter (File tF, FileOutputStream fos){
            value = 0;
            this.tF = tF;
            this.fos = fos;
            this.execute = true;
        }
        
        @Override
        public void run() {
            while(execute){
                try {
                    Thread.sleep(STEP);
                } catch (InterruptedException ex) { 
                }
                
                if (value >= READ_TIMEOUT)  recover();
                else                        value += STEP;
            }
        }
        
        public void stopExec(){
            this.execute = false;
        }
        
        public void recover (){     
            MediaPlayer mp = pc.getMediaPlayer();
            if (mp != null)  {
                mp.stop();
            }
            tF.delete();
            tF = new File (tempFile);
            

            try {
                fos = new FileOutputStream(tF, true);
            } catch (FileNotFoundException ex) {
            }
            
            try {
                Thread.sleep(DELAY_TIME);
            } catch (InterruptedException ex) {
            }

            if (mp != null){
                
                mp.play();
            }
            // PLAY AGAIN
        }
        
    }


        
    }
    /*
    public byte [] readNext(final DataInputStream in, long timeout, final int metaint)
            throws IOException, InterruptedException {

        final byte[] dataReady = new byte[metaint];
        final IOException[] maybeException = {null};
        final Thread reader = new Thread() {
            public void run() {                
                try {
                    in.readFully(dataReady, 0, metaint);
                } catch (ClosedByInterruptException e) {
                    System.err.println("Interr. exc. Reader interrupted.");
                } catch (IOException e) {
                    maybeException[0] = e;
                    System.err.println("io exc.");
                } catch (Exception e){
                    System.err.println("gral exc." + e);
                }
            }
        };

        Thread interruptor = new Thread() {
            public void run() {
                reader.interrupt();
            }
        };

        reader.start();
        for(;;) {

            reader.join(timeout);
            if (!reader.isAlive())
                break;

            interruptor.start();
            interruptor.join(1000);
            reader.join(1000);
            if (!reader.isAlive())
                break;

            System.err.println("We're hung");
            System.exit(1);
        }

        if ( maybeException[0] != null )
            throw maybeException[0];

        return dataReady;
    }*/

