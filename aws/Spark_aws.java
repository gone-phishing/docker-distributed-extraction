import java.io.*;
import java.util.logging.Logger;
import com.jcraft.jsch.*;
import org.json.*;
import java.util.*;

/**
 * Usage :
 * args[0] = "single" | "cluster"
 * args[1] = "username"
 * args[2] = "hostname"
 * args[3] = "privateKeyFile"
 * args[4] = "key_name"
 * args[5] = "image_id"
 * args[6] = "count"
 * args[7] = "instance_type"
 * args[8] = "security_groups"
 * args[9] = "instance_id1"
 * Example :
 * javac -cp ".:./lib/jsch.jar:./lib/json.jar" spark_aws.java
 * java -cp ".:./lib/jsch.jar" spark_aws single ec2-user <host-name> ~/Downloads/private_key.pem
 */

public class Spark_aws
{
	private static final Logger log = Logger.getLogger(Spark_aws.class.getName());
	private String privateKeyFile="";
	private String key_name="";
	private String username="";
	private String hostname="";
	private String instance_id1="";
	private String instance_type="";
	private String security_groups="";
	private String image_id="";
	private int count = 1;
	private Session session;

	public Spark_aws(String username, String hostname, String privateKeyFile, String key_name, String image_id, String count, String instance_type, String security_groups, String instance_id)
	{
		System.out.println("Single node setup");
		if(username.length() > 0) this.username = username;
		else this.username = "ec2-user";
		if(hostname.length() > 0) this.hostname = hostname;
		this.privateKeyFile = privateKeyFile;
		this.instance_id1 = instance_id;
		this.key_name = key_name;
		this.count = Integer.parseInt(count);
		this.instance_type = instance_type;
		this.security_groups = security_groups;
		this.image_id = image_id;

		try
		{
			System.out.println("[INFO] Launching single instance...");
			String iid = launch_single_instance(image_id);
			this.instance_id1 = iid;

			System.out.println("[INFO] Describe instance being executed");
			String public_dns_name = "";
			while(public_dns_name.length() == 0)
			{
				System.out.println("[INFO] Sleeping thread for 90 seconds while instance sets up... ");
				Thread.sleep(90000);
				public_dns_name = describe_instance("instance-id",iid);
			}
			this.hostname = public_dns_name;

			System.out.println("[INFO] Adding instance name tag");
			add_instance_tags(instance_id1,"Name","Deploy_Test3");

			System.out.println("[INFO] Runing commands on instance...");
			run_single_instance();

			Thread.sleep(3000);
			System.out.println("[INFO] Stopping the instance");
			stop_single_instance(iid);
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}


	}

	public void add_instance_tags(String id, String key, String tag)
	{
		String command = "aws ec2 create-tags --resources "+id+" --tags Key="+key+",Value="+tag;
		System.out.println("Command: "+command);
		String result[] = execute_command_shell(command);
		if(!result[0].equals("0"))
		{
			System.out.println("[ERROR] Failed to add tag to the instance");
			System.exit(0);
		}
		else System.out.println("[INFO] Tag added successfully");
	}

	public String launch_single_instance(String image_id)
	{
		String command = "aws ec2 run-instances --image-id "+image_id+" --count "+count+" --instance-type "+instance_type+" --key-name "+key_name ;
		System.out.println("Command: "+command);
		String result[] = execute_command_shell(command);
		if(!result[0].equals("0"))
		{
			System.out.println("[ERROR] Failed to launch image");
			System.exit(0);
		}
		else System.out.println("[INFO] Instance launched\n"+result[1]);

		JSONObject jb1 = new JSONObject(result[1]);
		JSONArray ja1 = (JSONArray) jb1.get("Instances");
		JSONObject jb2 = ja1.getJSONObject(0);
		String iid = jb2.getString("InstanceId");
		System.out.println("iid: "+iid);
		return iid;
	}

	public String describe_instance(String name, String value)
	{
		String command = "aws ec2 describe-instances --filters Name="+name+",Values="+value;
		System.out.println("Command: "+command);
		String result[] = execute_command_shell(command);
		if(!result[0].equals("0"))
		{
			System.out.println("[ERROR] Failed to describe instance");
			System.exit(0);
		}
		else System.out.println("[INFO] Instance description received\n"+result[1]);

		JSONObject jb1 = new JSONObject(result[1]);
		JSONArray ja1 = (JSONArray) jb1.get("Reservations");
		JSONObject jb2 = ja1.getJSONObject(0);
		JSONArray ja2 = (JSONArray) jb2.get("Instances");
		JSONObject jb3 = ja2.getJSONObject(0);
		String public_dns_name = jb3.getString("PublicDnsName");
		return public_dns_name;
	}

	public void stop_single_instance(String instance_id1)
	{
		String command = "aws ec2 stop-instances --instance-ids "+instance_id1;
		System.out.println("Command: "+command);
		String result[] = execute_command_shell(command);
		if(!result[0].equals("0"))
		{
			System.out.println("[ERROR] Failed to stop instance");
			System.exit(0);
		}
		else System.out.println("[INFO] Instance stopped\n"+result[1]);
	}

	public void terminate_single_instance()
	{
		String command = "aws ec2 terminate-instances --instance-ids "+instance_id1;
		String result[] = execute_command_shell(command);
		if(!result[0].equals("0"))
		{
			System.out.println("[ERROR] Failed to terminate instance");
			System.exit(0);
		}
		else System.out.println("[INFO] Instance terminated\n"+result[1]);
	}

	private String[] execute_command_shell(String command)
	{
		StringBuffer op = new StringBuffer();
		String out[] = new String[2];
		Process process;
		try
		{
			process = Runtime.getRuntime().exec(command);
			process.waitFor();
			int exitStatus = process.exitValue();
			out[0] = ""+exitStatus;
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null)
			{
				op.append(line + "\n");
			}
			out[1] = op.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return out;
	}

	public void run_single_instance()
	{
		try
		{
			Session session = getSession();
			session.connect();
			System.out.println("[INFO] Connected to the instance...");
			execute_command_aws(session, "mkdir deploy;");
			System.out.println("[INFO] Executed command 1");
			execute_command_aws(session, "wget --directory-prefix deploy/ https://github.com/gone-phishing/docker-distributed-extraction/archive/v0.1.1-beta.tar.gz");
			execute_command_aws(session, "cd deploy;tar -zxvf v0.1.1-beta.tar.gz;");
			execute_command_aws(session, "rm deploy/v0.1.1-beta.tar.gz");
			execute_command_aws(session, "deploy/docker-distributed-extraction-0.1.1-beta/util/check");
			session.disconnect();
			System.out.println("[INFO] Session disconnected...");
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
				new Spark_aws(args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
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