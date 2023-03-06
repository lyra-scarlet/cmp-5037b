import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class PlayerThread implements Runnable
{
   static AudioPlayer player;
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
               player.playBlock(blockQueue.poll());
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
         else
         {
            try
            {
               Thread.sleep(32);
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
            }
         }
      }
   }
}