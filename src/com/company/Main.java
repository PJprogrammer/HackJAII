package com.company;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Main
{
    RMIRegulatedMotor leftMotor;
    RMIRegulatedMotor rightMotor;
    RemoteEV3 ev3;

    public Main()
    {
        super();

        try {
            ev3 = new RemoteEV3("192.168.137.55");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        leftMotor = ev3.createRegulatedMotor("B", 'L');
        rightMotor = ev3.createRegulatedMotor("C", 'L');
    }

    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);

            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();

                (new Thread(new SerialReader(in))).start();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    /** */
    public class SerialReader implements Runnable
    {
        InputStream in;

        public SerialReader ( InputStream in )
        {
            this.in = in;
        }

        public void run ()
        {
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                while ( ( len = this.in.read(buffer)) > -1 )
                {
                    String test = new String(buffer,0,len);
                    System.out.println(new String(buffer,0,len));

                    if(test.contains("u")) {
                        System.out.println("Move foward");
                        rightMotor.setSpeed(200);
                        leftMotor.setSpeed(200);
                        leftMotor.forward();
                        rightMotor.forward();

                    }
                    if(test.contains("l")) {
                        System.out.println("Move left");
                        rightMotor.setSpeed(0);
                        leftMotor.setSpeed(200);
                        leftMotor.forward();


                    }
                    if(test.contains("r")) {
                        System.out.println("Move right");
                        rightMotor.setSpeed(200);
                        leftMotor.setSpeed(0);
                        rightMotor.forward();

                    }
                    if(test.contains("d")) {
                        System.out.println("Move down");
                        rightMotor.setSpeed(200);
                        leftMotor.setSpeed(100);
                        leftMotor.backward();
                        rightMotor.backward();

                    }
                    if(test.equals(null)) {
                        System.out.println("None");
                    }

                }

            }
            catch ( Exception e )
            {
                //e.printStackTrace();
                try {leftMotor.stop(true);
                    rightMotor.stop(true);
                    leftMotor.close();
                    rightMotor.close();}
                    catch (Exception he) {
                        he.printStackTrace();
                    }

            }
        }
    }

    public static void main ( String[] args )
    {
        try
        {
            (new Main()).connect("COM4");
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}