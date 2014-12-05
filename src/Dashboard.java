import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.FirmwareVersion;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.enums.Arm;
import com.thalmic.myo.enums.XDirection;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;
import lejos.pc.comm.NXTInfo;
@SuppressWarnings("serial")
public class Dashboard extends JFrame
{		
  public static ButtonHandler bh = new ButtonHandler();
  public static DataOutputStream outData;
  public static NXTConnector link;
  public static Myo myo;
  private static  JButton myoButton, robotButton;
  private static JLabel poseLabel, rollLabel, pitchLabel, stateLabel, speedLabel, steerLabel, statusLabel;
  private static float steeringFactor, speedFactor;
  private static boolean listening = true, botConnected = false, myoConnected = false;
  public Dashboard(){
	  setDefaultCloseOperation(EXIT_ON_CLOSE);
	  setTitle ("Myo Interpereter");
	  setBounds(650,350,500,500);
	  setLayout(new GridLayout(4,1));
	  add(makeButtons());
	  add(makeLabels());
	  add(makeSpinners());
	  statusLabel = new JLabel("Status", SwingConstants.CENTER);
	  add(statusLabel);
	  pack();
	  setVisible(true);
	  
  }
  private JPanel makeSpinners(){
	  JPanel p = new JPanel(new GridLayout(1,4));
	  JSpinner steerSpinner = new JSpinner(new SpinnerNumberModel(steeringFactor, -5, 5, 0.1));
	  steerSpinner.setToolTipText("Adjust scaling to control robot's speed");
	  p.add(new JLabel("Steer Scale"));
	  p.add(steerSpinner);
	  
	  JSpinner speedSpinner = new JSpinner(new SpinnerNumberModel(speedFactor, -5, 5, 0.1));
	  speedSpinner.setToolTipText("Adjust scaling to control robot's steering");
	  p.add(new JLabel("Speed Scale"));
	  p.add(speedSpinner);
	  p.setPreferredSize(p.getMinimumSize());
	  p.setMaximumSize(getMinimumSize());
	  return p;
  }
  private JPanel makeButtons(){
	  JPanel c = new JPanel(new GridLayout(1,2));
	  myoButton = new JButton("Myo");
	  myoButton.setActionCommand("myo-connect");
	  myoButton.addActionListener(bh);
	  c.add(myoButton);
	  robotButton = new JButton("Bot");
	  robotButton.setActionCommand("bot-connect");
	  robotButton.addActionListener(bh);
	  c.add(robotButton);
	  return c;
  }
  private JPanel makeLabels(){
	  JPanel c = new JPanel(new GridLayout(3, 2));
	  poseLabel = new JLabel("Pose", SwingConstants.CENTER);
	  poseLabel.setToolTipText("Your gesture as the Myo reports it");
	  pitchLabel = new JLabel("Pitch", SwingConstants.CENTER);
	  pitchLabel.setToolTipText("Your arm pivioting at the elbow");
	  rollLabel = new JLabel("Roll", SwingConstants.CENTER);
	  rollLabel.setToolTipText("Rotating your arm about your wrist");
	  stateLabel = new JLabel("State", SwingConstants.CENTER);
	  speedLabel = new JLabel("Speed", SwingConstants.CENTER);
	  speedLabel.setToolTipText("Robot's forward/backward speed");
	  steerLabel = new JLabel("Steer", SwingConstants.CENTER);
	  steerLabel.setToolTipText("Robot's steering ratio");
	  c.add(poseLabel);		c.add(stateLabel);
	  c.add(pitchLabel);	c.add(speedLabel);
	  c.add(rollLabel); 	c.add(steerLabel);
	  
	  return c;  
  }
  @SuppressWarnings("unused")
public static void main(String[] args){
	Dashboard NXTrc = new Dashboard();
  }//End main
  
  private static class ButtonHandler implements ActionListener{
    public void actionPerformed(ActionEvent ae){
      switch(ae.getActionCommand()){
      	case "myo-connect":
      		myoConnect();
      		break;
      	case "bot-connect":
      		botConnect();
      		break;
      }
     }//End ActionEvent(for buttons)


	private void myoConnect() {
		// TODO Auto-generated method stub
		
	}
  }//End ButtonHandler
  @SuppressWarnings("unused")
private static class MyoHandler implements DeviceListener{
	public void onPose(Myo myo, long timestamp, Pose pose) {
		try{
			switch(pose.getType()){
		
			case FIST:
				outData.writeUTF("drive");
				break;
			case FINGERS_SPREAD:
				if(listening)
					outData.writeUTF("stop");
				break;
			case WAVE_IN:
				break;
			case WAVE_OUT:
				break;
			case REST:
				break;
			case THUMB_TO_PINKY:
				listening = !listening;
				break;
			default:
				break;			
			} 
			outData.flush();
		}catch (IOException e) {
			e.printStackTrace();
		}	
	}
	@Override
	public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
		String steer = "steer:"+(rotation.getW() * steeringFactor);
		String speed = "speed"+(rotation.getY() * speedFactor);
		try {
			outData.write(steer.getBytes());
			outData.write(speed.getBytes());
		} catch (IOException e) {
			System.err.println("Error talking to NXT");
			e.printStackTrace();
		}
	}
	//unused
	public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {}
	public void onPair(Myo myo, long timestamp, FirmwareVersion firmwareVersion) {}
	public void onUnpair(Myo myo, long timestamp) {}
	public void onArmSync(Myo myo, long timestamp, Arm arm,XDirection xDirection) {}
	public void onArmUnsync(Myo myo, long timestamp) {}
	public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {}
	public void onRssi(Myo myo, long timestamp, int rssi) {}
	public void onConnect(Myo myo, long timestamp,FirmwareVersion firmwareVersion) {}
	public void onDisconnect(Myo myo, long timestamp) {}	
  }
  public static void  botConnect(){
	 NXTComm nxtComm;
	try {
		System.out.println("Start");
		nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
		System.out.println("hi");
		NXTInfo[] nxtInfo = nxtComm.search("NXT");
		if(nxtInfo.length < 1){
			statusLabel.setText("NXT not found!");
			Thread.sleep(3000);
			statusLabel.setText("");
			return;
		}	
		System.out.println(nxtInfo);
		nxtComm.open(nxtInfo[0]);
		outData = new DataOutputStream(nxtComm.getOutputStream());
		System.out.println(outData);
		// TODO: Update button
	    statusLabel.setText("\nNXT is Connected"); 
	} catch (NXTCommException | InterruptedException e) {
		statusLabel.setText("Error Connecting to NXT");
		e.printStackTrace();
		return;
	}  
     
  }//End connect
  
  public static void disconnect()
  {
     try{
        outData.close();
        link.close();
        } 
     catch (IOException ioe) {
        System.out.println("\nIO Exception writing bytes");
     }
     System.out.println("\nClosed data streams");
     
  }//End disconnect
}//End ControlWindow class
