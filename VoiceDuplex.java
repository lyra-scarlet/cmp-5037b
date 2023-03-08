public class VoiceDuplex
{
   public static void main (String[] args)
   {
      VoiceSenderThread sender = new VoiceSenderThread();

      int socket = Config.getInt("socket");
      if (socket == 1)
      {
         System.out.println("USING RECEIVER1\n");
         VoiceReceiverSocket1 receiver1 = new VoiceReceiverSocket1();
         receiver1.start();
      }
      else
      {
         VoiceReceiverThread receiver = new VoiceReceiverThread();
         receiver.start();
      }

      sender.start();
   }
}
