package com.convexcreative.pposcinterpreter;

import com.bulenkov.darcula.DarculaLaf;
import com.convexcreative.pposcinterpreter.data.ValueManager;
import com.convexcreative.pposcinterpreter.obj.ClipData;
import com.convexcreative.pposcinterpreter.obj.ProjectData;
import com.convexcreative.pposcinterpreter.swing.ConvexFrame;
import com.convexcreative.propres.ProPresAPI;
import com.convexcreative.propres.ProPresAPIConfig;
import com.convexcreative.propres.event.slide.SlideChangeEvent;
import com.convexcreative.propres.serializable.Slide;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPortOut;

import javax.swing.*;
import java.awt.image.AreaAveragingScaleFilter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

   /* public static void main(String... args) throws IOException {

        ProPresAPIConfig ppConfig = new ProPresAPIConfig("127.0.0.1:59213", "1234", ProPresAPIConfig.V6);
        ProPresAPI ppAPI = ProPresAPI.getInstance(ppConfig);

        final OSCPortOut portOut = new OSCPortOut(new InetSocketAddress(InetAddress.getLocalHost(), 25565));


        System.out.println("OSC Server Initialized!");
        final ArrayList<String> text = new ArrayList<>();

        ppAPI.registerRecurringEvent(new SlideChangeEvent() {
            @Override
            public void run() {
                Slide curSlide = (Slide) getEventMetadata()[0];
                text.clear();
                String formattedText = "";
                for(String s : curSlide.getSplitSlideText()){
                    formattedText += s.toUpperCase() + "\n";
                }

                text.add(formattedText);

                final OSCMessage msg = new OSCMessage("/composition/layers/3/clips/1/video/source/textgenerator/text/params/lines", text);
                try {
                    portOut.send(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (OSCSerializeException e) {
                    e.printStackTrace();
                }
            }
        });


    }*/

   private ValueManager valueManager;

   private static Main instance;

   public Main(){
      init();
   }

   public void init(){
      instance = this;

      valueManager = new ValueManager();

      valueManager.update("pphost", ProjectData.DEF_HOST);
      valueManager.update("ppport", ProjectData.DEF_PRO_PORT);
      valueManager.update("pppass", ProjectData.DEF_PRO_PASSWORD);

      valueManager.update("oschost", ProjectData.DEF_HOST);
      valueManager.update("oscport", ProjectData.DEF_OSC_PORT);

      try {
         UIManager.setLookAndFeel(new DarculaLaf());
      } catch (UnsupportedLookAndFeelException e) {
         System.out.println("Could not load Darcula LAF...");
      }

      ConvexFrame frame = new ConvexFrame(new Main());


      ProPresAPIConfig config = new ProPresAPIConfig("127.0.0.1:59213", "1234", ProPresAPIConfig.V6);
      ProPresAPI.getInstance(config);

   }

   public static void main(String... args){
      new Main();
   }

   public ValueManager getValueManager(){
      return valueManager;
   }

   public static Main getInstance(){
      return instance;
   }

}
