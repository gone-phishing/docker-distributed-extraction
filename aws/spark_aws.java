import java.io.*;
import java.util.logging.Logger;
import com.jcraft.jsch.*;

/**
 * Usage :
 * args[0] = "single" | "cluster"
 * args[1] = "username"
 * args[2] = "hostname"
 * args[3] = "privateKeyFile"
 */

class spark_aws
{
	private static final Logger log = Logger.getLogger(spark_aws.class.getName());
	private String privateKeyFile="";
	private String username="";
	private String hostname="";

	spark_aws(String username, String hostname, String privateKeyFile)
	{
		System.out.println("Single node setup");
		this.username = username;
		this.hostname = hostname;
		this.privateKeyFile = privateKeyFile;
		run_single_instance();
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
		if(args[0].equals("single"))
		{
			System.out.println("Single node setup");
			spark_aws(args[1],args[2]);
		}
		else if(args[0].equals("cluster"))
		{
			System.out.println("Initiate cluster setup");
		}
		else
		{
			System.out.println("Correct format of execution is: \n<setup-type> : \"single\" or \"cluster\" ");
		}
		new spark_aws();
	}
}