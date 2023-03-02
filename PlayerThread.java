import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import static java.lang.Thread.sleep;

public class PlayerThread extends Thread
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

   static Queue<byte[]> sharedQueue = new LinkedList<>();

   public PlayerThread(Queue<byte[]> myQueue)
   {
      sharedQueue = myQueue;
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
      synchronized (sharedQueue)
      {
         while (!sharedQueue.isEmpty())
         {
            byte[] block = sharedQueue.poll();
            setBlock(block);
            try
            {
               player.playBlock(block);
               sharedQueue.wait();
            }
            catch (InterruptedException | IOException e)
            {
               e.printStackTrace();
            }
         }
      }
   }
}
