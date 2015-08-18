package sample;

import java.io.*;
import java.util.logging.Logger;
import com.jcraft.jsch.*;
//import com.jcraft.jsch.JSchException;

/**
 * Usage :
 * args[0] = "single" | "cluster"
 * args[1] = "username"
 * args[2] = "hostname"
 * args[3] = "privateKeyFile"
 */

public class spark_aws
{
	private static final Logger log = Logger.getLogger(spark_aws.class.getName());
	private String privateKeyFile="";
	private String username="";
	private String hostname="";
	private Session session;

	public spark_aws(String username, String hostname, String privateKeyFile)
	{
		System.out.println("Single node setup");
		this.username = username;
		this.hostname = hostname;
		this.privateKeyFile = privateKeyFile;
		run_single_instance();
	}

	public void run_single_instance()
	{
		try
		{
			Session session = getSession();
			session.connect();
			execute_sudo(session, "pwd;", "");
			session.disconnect();
		}
		catch(JSchException je)
		{
			je.printStackTrace();
		}
	}

	public Session execute_sudo(Session session, String command, String passwd)
	{
		try
		{
			ChannelExec channel=(ChannelExec) session.openChannel("exec");
	      	//channel.setCommand("sudo -S -p '' "+command);
			channel.setCommand(command);
			//BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
			InputStream in=channel.getInputStream();
	      	OutputStream out=channel.getOutputStream();
	      	channel.setErrStream(System.err);

	      	channel.connect();

	      	out.write((passwd+"\n").getBytes());
	      	out.flush();

	      	byte[] tmp=new byte[1024];
	      	while(true)
	      	{
	      	  	while(in.available()>0)
	      	  	{
	      	    	int i=in.read(tmp, 0, 1024);
	      	    	if(i<0)break;
	      	    	System.out.print(new String(tmp, 0, i));
	      	  	}
	      	  	if(channel.isClosed())
	      	  	{
	      	  		if(channel.getExitStatus() == 0)
	      	  		{
	      	  			System.out.println("Execution successful");
	      	  		}
	      	  		else
	      	  		{
	      	  			System.out.println("exit-status: "+channel.getExitStatus());
	      	  		}
	      	    	break;
	        	}
	        	try
	        	{
	        		Thread.sleep(1000);
	        	}
	        	catch(Exception ee)
	        	{
	        		ee.printStackTrace();
	        	}
	      	}
			channel.disconnect();
	    }
	    catch(JSchException jex)
	    {
	      	jex.printStackTrace();
	    }
	    catch(IOException iex)
	    {
	    	iex.printStackTrace();
	    }

	    return session;
	}

	private Session getSession() throws JSchException
	{
	  	if (this.session != null && this.session.isConnected())
	  	{
	    	return this.session;
		}
	  	JSch jsch=new JSch();
	  	jsch.addIdentity(privateKeyFile);
		this.session=jsch.getSession(username,hostname);
		this.session.setConfig("StrictHostKeyChecking","no");
		//this.session.setConfig("UserKnownHostsFile","/dev/null");
		return this.session;
	}

	public static void main(String[] args)
	{
		if(args.length >= 4)
		{
			if(args[0].equals("single"))
			{
				new spark_aws(args[1], args[2], args[3]);
			}
			else if(args[0].equals("cluster"))
			{
				System.out.println("Initiate cluster setup");
			}
		}
		else
		{
			System.out.println("Correct format of execution is: \n<setup-type> : [\"single\"] | [\"cluster\"] <username> <hostname> <privateKeyFile>");
		}
		System.out.println("That's all folks !!");
	}
}