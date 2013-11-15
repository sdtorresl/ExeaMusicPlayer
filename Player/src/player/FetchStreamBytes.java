package player;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sdtorresl
 */
public class FetchStreamBytes implements Runnable {
    private String tempFile;
    private String streamURL;
    private volatile boolean execute;

    public FetchStreamBytes (String tempFile, String streamURL){
        this.tempFile = tempFile;
        this.streamURL = streamURL;
        this.execute = true;
    }
    
    public void stopExecuting(){
        this.execute = false;
    }
    
    @Override
    public void run() {
        File tF = null;
        FileOutputStream fos = null;
        
        tF = new File (tempFile);
        
        try {
            fos = new FileOutputStream(tF, true);
        } catch (FileNotFoundException ex) {
            /*
            PLACE CODE HERE TO HANDLE THE EXCEPTION
            */
        }
        
        try
        {
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
            /** You could specify some other condition here **/
            while (stream != null && execute)
            {
                /** Reading Audio data **/
                stream.readFully(audioBuffer, 0, metaint);
                /** Reading first byte after our audio data **/
                int headerByte = stream.read();
                /** We only read metadata if header byte is bigger than 0, if it is 0 this means there is no metadata **/
                if (headerByte > 0)
                {
                    /** Getting our metadata header length **/
                    int headerLength = headerByte * 16;
                    /** Creating byte array which is equal to header size and will store our title **/
                    byte[] titleBuffer = new byte[headerLength];
                    /** Reading title into buffer **/
                    stream.readFully(titleBuffer, 0, headerLength);
                    /** Printing out our title **/
                }
                fos.write(audioBuffer);
            }
               
            // following operations are supossed to be done in the main thread
            fos.flush();
            fos.close();
            tF.delete();
        }
        catch (IOException e)
        {
            
        } catch (NumberFormatException e) {
            
        }
    } 
}
