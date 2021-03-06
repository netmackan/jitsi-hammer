/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.utils;


import org.jitsi.hammer.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.greyfading.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.ivffile.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.rtpdumpfile.*;
import org.jitsi.service.libjitsi.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.codec.*;
import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.MediaFormatFactory;
import org.jitsi.videobridge.*;


/**
 * This class is used to get the chosen MediaDevice for a given MediaType.
 * 
 * The choice is based on the option/argument given to the program (parsed
 * and stored in a <tt>CmdLineArguments</tt>).
 *
 * @author Thomas Kuntz
 */
public class MediaDeviceChooser
{
    /**
     * The chosen video MediaDevice of this <tt>MediaDeviceChooser</tt>
     */
    MediaDevice videoMediaDevice;
    
    /**
     * The chosen audio MediaDevice of this <tt>MediaDeviceChooser</tt>
     */
    MediaDevice audioMediaDevice;
    
    /**
     * Initialize an empty <tt>MediaDeviceChooser<tt>. No video or audio
     * MediaDevice will be chosen (they'll need to be set later).
     */
    public MediaDeviceChooser()
    {
        this(null);
    }
    
    /**
     * Initialize a <tt>MediaDeviceChooser</tt> based on a <tt>CmdLineArguments</tt>
     * (so based on the arguments/options the user gave to the program).
     * 
     * @param cmdArg the <tt>CmdLineArguments</tt> containing the arguments/options
     * of the program, on which the audio and video MediaDevice will be chosen.
     */
    public MediaDeviceChooser(CmdLineArguments cmdArg)
    {
        if(cmdArg != null)
        {
            MediaService service = LibJitsi.getMediaService();
            MediaFormatFactory factory = service.getFormatFactory();
            
            /*
             * If an rtpdump file is given, it has priority over
             * AudioSilence
             */
            if(cmdArg.getAudioRtpdumpFile() != null)
            {
                audioMediaDevice = RtpdumpMediaDevice.createRtpdumpMediaDevice(
                        cmdArg.getAudioRtpdumpFile(),
                        Constants.OPUS_RTP,
                        factory.createMediaFormat("Opus", 48000, 2));
            }
            else
            {
                audioMediaDevice = new AudioSilenceMediaDevice();
            }
            
            
            /*
             * For the video MediaDevice, an rtpdump CaptureDevice has priority
             * over an ivf CaptureDevice that has priority over
             * the VideoGreyFading CaptureDevice.
             */
            if(cmdArg.getVideoRtpdumpFile() != null)
            {
                videoMediaDevice = RtpdumpMediaDevice.createRtpdumpMediaDevice(
                        cmdArg.getVideoRtpdumpFile(),
                        Constants.VP8_RTP,
                        factory.createMediaFormat("vp8", 90000));
            }
            else if(cmdArg.getIVFFile() != null)
            {
                videoMediaDevice = new IVFMediaDevice(cmdArg.getIVFFile());
            }
            else
            {
                videoMediaDevice = new VideoGreyFadingMediaDevice();
            }
        }
    }
    
    /**
     * Get the chosen <tt>MediaDevice</tt> from a <tt>MediaType</tt>
     * @return the chosen <tt>MediaDevice</tt>
     */
    public synchronized MediaDevice getMediaDevice(MediaType type)
    {
        MediaDevice returnedDevice = null;
        switch(type)
        {
            case AUDIO:
                returnedDevice = audioMediaDevice;
                break;
            case VIDEO:
                returnedDevice = videoMediaDevice;
                break;
            default:
                break;
        }
        
        return returnedDevice;
    }

    /**
     * Set a <tt>MediaDevice</tt> as the chosen audio or video <tt>MediaDevice<tt>
     * (depending of its <tt>MediaType</tt>)
     * @param dev the <tt>MediaDevice</tt> that will be considered chosen.
     */
    public synchronized void setMediaDevice(MediaDevice dev)
    {
        if(dev != null)
        {
            switch(dev.getMediaType())
            {
                case VIDEO:
                    videoMediaDevice = dev;
                    break;
                case AUDIO:
                    audioMediaDevice = dev;
                    break;
            default:
                break;
            }
        }
    }
}
