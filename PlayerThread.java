import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
public class PlayerThread implements Runnable
{
   static byte[] block = new byte[512];
   static AudioPlayer player;
   static
   {
      try
      {
         player = new AudioPlayer();
      }
      catch (LineUnavailableException e)
      {
         e.printStackTrace();
      }
   }

   public void setBlock(byte[] newBlock)
   {
      block = newBlock;
   }

   public void start()
   {
      Thread thread = new Thread(this);
      thread.start();
   }

   public void run()
   {
      while (true)
      {
         try
         {
            player.playBlock(block);
            Thread.sleep(180, 2);
         }
         catch (IOException | InterruptedException e)
         {
            e.printStackTrace();
         }
      }
   }
}
