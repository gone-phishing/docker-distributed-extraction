import java.io.*;
import java.util.logging.Logger;
import com.jcraft.jsch.*;
import org.json.*;
import java.util.*;

/**
 * Usage (for single instance):
 * args[0] 	= "single" | "cluster"
 * args[1] 	= "username"
 * args[2] 	= "hostname"
 * args[3] 	= "privateKeyFile"
 * args[4] 	= "key_name"
 * args[5] 	= "image_id"
 * args[6] 	= "instance_count"
 * args[7] 	= "instance_type"
 * args[8] 	= "security_groups"
 * args[9] 	= "instance_id1"
 * args[10] = "setup-timeout"
 *
 * Usage (for cluster setup):
 * args[0] = "single" | "cluster"
 * args[1] = "cluster_name"
 * args[2] = "ami_version"
 * args[3] = "application_name"
 * args[4] = "key_name"
 * args[5] = "instance_type"
 * args[6] = "instance_count"
 * args[7] = "username"
 * args[8] = "hostname"
 * args[9] = "privatekeyfile"
 * args[10] = "setup-timeout"
 *
 * Example :
 * javac -cp ".:./lib/jsch.jar:./lib/json.jar" Spark_aws.java
 * java -cp ".:./lib/jsch.jar:./lib/json.jar" Spark_aws single <username> <hostname> $HOME/Downloads/gsoc1.pem <keyname> ami-5189a661 1 t2.micro "" <imageid>
 * java -cp ".:./lib/jsch.jar:./lib/json.jar" Spark_aws cluster "<clustername>" 3.8 Spark <keyname> m3.xlarge 3 hadoop <hostname>
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
	private String cluster_name="";
	private String ami_version="";
	private String application_name="";
	private String cluster_id = "";
	private long SETUP_INTERVAL= 1000;
	private int instance_count = 1;
	private Session session;

	public Spark_aws(String cluster_name, String ami_version, String application_name, String key_name, String instance_type, String instance_count, String username, String hostname, String privatekeyfile, String timeout)
	{
		System.out.println("[INFO] Spark Cluster setup");
		this.cluster_name = cluster_name;
		this.ami_version = ami_version;

		if(application_name.length() == 0) this.application_name = "Spark";
		else this.application_name = application_name;

		this.key_name = key_name;
		this.instance_type = instance_type;
		this.instance_count = Integer.parseInt(instance_count);
		this.privateKeyFile = privatekeyfile;

		if(username.length() > 0) this.username = username;
		else this.username = "hadoop";

		if(hostname.length() > 0) this.hostname = hostname;

		if(timeout.length() == 0) this.SETUP_INTERVAL = 40000;
		else this.SETUP_INTERVAL = Long.parseLong(timeout);

		try
		{
			System.out.println("[INFO] Launching spark cluster...");
			String cluster_id = launch_spark_cluster();
			this.cluster_id = cluster_id;

			System.out.println("[INFO] Describe instance being executed...");
			String master_pub_dns = "";
			while( master_pub_dns.length() == 0 )
			{
				System.out.println("[INFO] Sleeping thread for "+(SETUP_INTERVAL/1000)+" seconds while cluster sets up... ");
				Thread.sleep(SETUP_INTERVAL);
				master_pub_dns = describe_cluster(cluster_id);
			}
			this.hostname = master_pub_dns;
			System.out.println("hostname: "+this.hostname);

			System.out.println("[INFO] Installing maven on the master node");
			clsuter_maven_install();

			System.out.println("[INFO] Execute project build and deployment");
			run_cluster_operations();

			// System.out.println("[INFO] Terminating instances...");
			// String cluster_id_array[] = new String[1]; // Assuming in future multiple cluster support can be required
			// cluster_id_array[0] = cluster_id;
			// terminate_clusters(cluster_id_array);

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public Spark_aws(int single, String username, String hostname, String privateKeyFile, String key_name, String image_id, String instance_count, String instance_type, String security_groups, String instance_id, String timeout)
	{
		System.out.println("[INFO] Single node setup");
		if(hostname.length() > 0) this.hostname = hostname;
		this.privateKeyFile = privateKeyFile;
		this.instance_id1 = instance_id;
		this.key_name = key_name;
		this.instance_count = Integer.parseInt(instance_count);
		this.instance_type = instance_type;
		this.security_groups = security_groups;
		this.image_id = image_id;
		if( image_id.equals("ami-e7527ed7") ) this.username = "ec2-user";
		else if( image_id.equals("ami-5189a661") ) this.username = "ubuntu";
		else this.username = username;

		if(timeout.length() == 0) this.SETUP_INTERVAL = 40000;
		else this.SETUP_INTERVAL = Long.parseLong(timeout);

		try
		{
			System.out.println("[INFO] Launching single instance...");
			String iid = launch_single_instance(image_id);
			this.instance_id1 = iid;

			System.out.println("[INFO] Describe instance being executed");
			String public_dns_name = "";
			while(public_dns_name.length() == 0)
			{
				System.out.println("[INFO] Sleeping thread for "+(SETUP_INTERVAL/1000)+" seconds while instance sets up... ");
				Thread.sleep(SETUP_INTERVAL);
				public_dns_name = describe_instance("instance-id",iid);
			}
			this.hostname = public_dns_name;

			System.out.println("[INFO] Adding instance name tag");
			add_instance_tags(instance_id1,"Name","Deploy_Test10");

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

	public String launch_spark_cluster()
	{
		String command = "aws emr create-cluster --name \""+cluster_name+"\" --ami-version "+ami_version+" --applications Name="+application_name+" --ec2-attributes KeyName="+key_name+" --instance-type "+instance_type+" --instance-count "+instance_count+" --use-default-roles";
		System.out.println("Command: "+command);
		String result[] = execute_command_shell(command);
		if(!result[0].equals("0"))
		{
			System.out.println("[ERROR] Failed to launch cluster");
			System.exit(0);
		}
		else System.out.println("[INFO] Cluster launched\n"+result[1]);
		JSONObject jb1 = new JSONObject(result[1]);
		String cluster_id = jb1.getString("ClusterId");
		return cluster_id;
	}

	public void clsuter_maven_install()
	{
		try
		{
			Session session = getSession();
			session.connect();
			System.out.println("[INFO] Connected to the instance...");
			execute_command_aws(session, "wget http://mirrors.gigenet.com/apache/maven/maven-3/3.3.3/binaries/apache-maven-3.3.3-bin.tar.gz;");
			execute_command_aws(session, "tar -zxvf apache-maven-3.3.3-bin.tar.gz; mv apache-maven-3.2.3 /usr/local/;");
			System.out.println("[INFO] Extraction of maven tar file successfull");
			execute_command_aws(session, "cd /usr/local/; sudo -t ln -s apache-maven-3.2.3 maven;");
			System.out.println("[INFO] sudo operation performed successfully");
			execute_command_aws(session, "cd /etc/profile.d/; sudo -t echo \"\" > maven.sh; sudo -t echo \"export M2_HOME=/usr/local/maven\" >> maven.sh; sudo -t echo \"export M2=$M2_HOME/bin\" >> maven.sh; sudo -t echo \"PATH=$M2:$PATH\" >> maven.sh");

			session.disconnect();
			System.out.println("[INFO] Session disconnected...");
		}
		catch(JSchException je)
		{
			je.printStackTrace();
		}
	}

	public void run_cluster_operations()
	{
		try
		{
			Session session = getSession();
			session.connect();
			System.out.println("[INFO] Connected to the instance...");
			execute_command_aws(session, "mvn -version");
			execute_command_aws(session, "mkdir dbpedia;");
			System.out.println("[INFO] Executed first command on the instance after ssh");
			execute_command_aws(session, "wget --directory-prefix dbpedia/ https://github.com/gone-phishing/distributed-extraction-framework/archive/spark_1.3.0-update.zip");
			execute_command_aws(session, "cd dbpedia;unzip spark_1.3.0-update.zip;");
			execute_command_aws(session, "rm dbpedia/spark_1.3.0-update.zip");
			execute_command_aws(session, "mv distributed-extraction-framework-spark_1.3.0-update distributed-extraction-framework;");
			execute_command_aws(session, "cd dbpedia/distributed-extraction-framework; mvn clean install -Dmaven.test.skip=true;");
			execute_command_aws(session, "cd dbpedia/distributed-extraction-framework; ./run download distconfig=download/src/test/resources/dist-download.properties config=download/src/test/resources/download.properties;");
			System.out.println("[INFO] If you don't imagine, nothing ever happens at all :)");
			session.disconnect();
			System.out.println("[INFO] Session disconnected...");
		}
		catch(JSchException je)
		{
			je.printStackTrace();
		}
	}

	public void terminate_clusters(String[] cluster_ids)
	{
		String command = "aws emr terminate-clusters --cluster-ids ";
		for(int i=0;i<cluster_ids.length; i++)
		{
			if(i == cluster_ids.length - 1) command += cluster_ids[i]+"";
			else command += cluster_ids[i]+" ";
		}
		System.out.println("Command: "+command);
		String result[] = execute_command_shell(command);
		if(!result[0].equals("0"))
		{
			System.out.println("[ERROR] Failed to terminate cluster instances");
			System.exit(0);
		}
		else System.out.println("[INFO] Instance specified terminated\n"+result[1]);
	}

	public String describe_cluster(String cluster_id)
	{
		String command = "aws emr describe-cluster --cluster-id "+cluster_id;
		System.out.println("Command: "+command);
		String result[] = execute_command_shell(command);
		if(!result[0].equals("0"))
		{
			System.out.println("[ERROR] Failed to describe cluster");
			System.exit(0);
		}
		else System.out.println("[INFO] Cluster description received\n"+result[1]);

		JSONObject jb1 = new JSONObject(result[1]);
		JSONObject jb2 = jb1.getJSONObject("Cluster");
		System.out.println("Cluster info: "+jb2.toString());
		String master_pub_dns = jb2.get("MasterPublicDnsName").toString();
		if( master_pub_dns.equals("null") ) master_pub_dns = "";
		System.out.println("Master dns: "+master_pub_dns);
		return master_pub_dns;
	}

	public String launch_single_instance(String image_id)
	{
		String command = "aws ec2 run-instances --image-id "+image_id+" --count "+instance_count+" --instance-type "+instance_type+" --key-name "+key_name ;
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
		System.out.println("[INFO] Instance id: "+iid);
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

	/**
	 * @id : Instance-id
	 * @key : Key of the tag
	 * @tag : Value for the key to be added
	 * The method adds tags to the instance made
	 */
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
			System.out.println("[INFO] Executed first command on the instance after ssh");
			execute_command_aws(session, "wget --directory-prefix deploy/ https://github.com/gone-phishing/docker-distributed-extraction/archive/v0.2.4-beta.tar.gz");
			execute_command_aws(session, "cd deploy;tar -zxvf v0.2.4-beta.tar.gz;");
			execute_command_aws(session, "rm deploy/v0.2.4-beta.tar.gz");
			execute_command_aws(session, "deploy/docker-distributed-extraction-0.2.4-beta/util/check");
			execute_command_aws(session, "deploy/docker-distributed-extraction-0.2.4-beta/util/docker_installer");
			System.out.println("[INFO] Going to build the docker image");
			execute_command_aws(session, "cd deploy/docker-distributed-extraction-0.2.4-beta/;sudo docker build -t gonephishing/dbpedia .");
			System.out.println("[INFO] If you don't imagine, nothing ever happens at all :)");
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
	      	  			System.out.println("[ERROR] Why do we fall, Bruce? So we can learn to pick ourselves up again :p");
	      	  			System.exit(0);
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
	      	channel.setCommand("sudo -S -t -p '' "+command);
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
		this.session.setConfig("UserKnownHostsFile","/dev/null");
		return this.session;
	}

	private static void correct_execution_format()
	{
		System.out.println("[ERROR] Correct format of execution is: \n[INFO] <setup-type> : [\"single\" | \"cluster\"] <username> <hostname> <privateKeyFile> ...");
		System.out.println("[ERROR] Check automation script / documentation for more information");
	}

	public static void main(String[] args)
	{
		if(args.length >= 4)
		{
			if(args[0].equals("single"))
			{
				new Spark_aws(1, args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[args.length - 1]);
			}
			else if(args[0].equals("cluster"))
			{
				new Spark_aws(args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[args.length - 1]);
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