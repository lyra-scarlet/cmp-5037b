public class VoiceDuplex
{
   public static void main (String[] args)
   {
      VoiceReceiverThread receiver = new VoiceReceiverThread();
      VoiceSenderThread sender = new VoiceSenderThread();

      receiver.start();
      sender.start();
   }
}