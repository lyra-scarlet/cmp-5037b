import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class PlayerThread implements Runnable
{
   static AudioPlayer player;
   static byte[] block = new byte[512];
   static Queue<byte[]> blockQueue = new LinkedList<>();
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

   public void addToQueue(byte[] block) { blockQueue.add(block); }

   public void start()
   {
      Thread thread = new Thread(this);
      thread.start();
   }

   public void run()
   {
      while (true)
      {
         if (!blockQueue.isEmpty())
         {
            try
            {
               block = blockQueue.poll();
               player.playBlock(block);
               Thread.sleep(5);
            }
            catch (IOException | InterruptedException e)
            {
               e.printStackTrace();
            }
         }
         else
         {
            try
            {
               for (int i = 0; i < block.length; i++)
               {
                  if (block[i] < 0)
                  {
                     block[i] *= -0.7;
                  }
                  else {
                     block[i] *= 0.7;
                  }
               }
               player.playBlock(block);
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }
   }
}
