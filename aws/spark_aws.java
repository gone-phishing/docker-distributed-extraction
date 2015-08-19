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
 * args[4] = "instance_id1"
 *
 * Example :
 * javac -cp ".:./lib/jsch.jar" spark_aws.java
 * java -cp ".:./lib/jsch.jar" spark_aws single ec2-user <host-name> ~/Downloads/private_key.pem
 */

public class spark_aws
{
	private static final Logger log = Logger.getLogger(spark_aws.class.getName());
	private String privateKeyFile="";
	private String username="";
	private String hostname="";
	private String instance_id1="";
	private Session session;

	public spark_aws(String username, String hostname, String privateKeyFile, String instance_id)
	{
		System.out.println("Single node setup");
		this.username = username;
		this.hostname = hostname;
		this.privateKeyFile = privateKeyFile;
		this.instance_id1 = instance_id;
		//launch_single_instance();
		//run_single_instance();
		terminate_single_instace();
	}

	public void terminate_single_instace()
	{
		String command = "aws ec2 terminate-instances --instance-ids "+instance_id1;
		String result = execute_command_shell(command);
		if(result.charAt(0) != '0')
		{
			System.out.println("[ERROR] Failed to terminate instance");
		}
		else System.out.println("[INFO] Instance terminated\n"+result);
	}

	private String execute_command_shell(String command)
	{
		StringBuffer op = new StringBuffer();

		Process process;
		try
		{
			process = Runtime.getRuntime().exec(command);
			process.waitFor();
			int exitStatus = process.exitValue();
			op.append("Exit Status: "+exitStatus+"\n");
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null)
			{
				op.append(line + "\n");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return op.toString();
	}

	public void run_single_instance()
	{
		try
		{
			Session session = getSession();
			session.connect();
			execute_command_aws(session, "mkdir deploy;");
			execute_command_aws(session, "wget --directory-prefix deploy/ https://github.com/gone-phishing/docker-distributed-extraction/archive/v0.1.1-beta.tar.gz");
			execute_command_aws(session, "cd deploy;tar -zxvf v0.1.1-beta.tar.gz;");
			execute_command_aws(session, "rm deploy/v0.1.1-beta.tar.gz");
			execute_command_aws(session, "deploy/docker-distributed-extraction-0.1.1-beta/util/check");
			session.disconnect();
		}
		catch(JSchException je)
		{
			je.printStackTrace();
		}
	}

	public Session execute_command_aws(Session session, String command)
	{
		try
		{
			ChannelExec channel=(ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			//BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
			InputStream in=channel.getInputStream();
	      	channel.setErrStream(System.err);

	      	channel.connect();

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
	      	  			System.out.println("[INFO] Command: "+command+" Execution successful");
	      	  		}
	      	  		else
	      	  		{
	      	  			System.out.println("[ERROR] Command failed with exit-status: "+channel.getExitStatus());
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

	public Session execute_command_aws_sudo(Session session, String command, String passwd)
	{
		try
		{
			ChannelExec channel=(ChannelExec) session.openChannel("exec");
	      	channel.setCommand("sudo -S -p '' "+command);
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

	private static void correct_execution_format()
	{
		System.out.println("[INFO] Correct format of execution is: \n[INFO] <setup-type> : [\"single\" | \"cluster\"] <username> <hostname> <privateKeyFile>");
	}

	public static void main(String[] args)
	{
		if(args.length >= 4)
		{
			if(args[0].equals("single"))
			{
				new spark_aws(args[1], args[2], args[3], args[4]);
			}
			else if(args[0].equals("cluster"))
			{
				System.out.println("Initiate cluster setup");
			}
			else
			{
				System.out.println("[ERROR] Setup type can only be 'single' or 'cluster'");
				correct_execution_format();
			}
		}
		else
		{
			System.out.println("[ERROR] Insufficient arguments");
			correct_execution_format();
		}
		System.out.println("That's all folks !!");
	}
}