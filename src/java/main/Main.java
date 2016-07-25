

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.stream.Stream;

import javax.naming.ldap.LdapName;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Main {

	public static void main(String[] args) {
		try {
			new Main().run(args);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private void run(String[] args) throws Exception {
		// make sure there is a hostname
		if (null == args || args.length == 0 || null == args[0] || args[0].length() == 0) {
			System.out.println("*** ERROR - Missing argument. "); 
			System.out.println("Specify the hostname to connect to with protocol etc. as a parameter.");
			System.out.println("");
			System.out.println("Syntax:");
			System.out.println("java -cp . Main <hostname>");
			System.out.println("");
			System.out.println("Example:");
			System.out.println("java -cp . Main https://www.google.com");
			System.out.println("");
			return;
		}
		
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { 
		    new X509TrustManager() {     
		    	public X509Certificate[] getAcceptedIssuers() {
		        	return null;
		        } 
		        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
		        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		        	Stream.of(certs).forEach(cert -> {
		        		String cn = null;
		        		String name = null;
		        		try {
		        			LdapName ldapname = new LdapName(cert.getSubjectDN().toString());
		        			cn = ldapname.get(ldapname.getRdns().size()-1);
		        			name = cn;
		        			int idx = cn.indexOf('=');
		        			if (idx >= 0) name = cn.substring(idx+1);
		        			
		        		} catch (Throwable T) {
		        			System.out.println("*** ERROR - unable to process certificate subject name <" + cert.getSubjectDN() + "> - ignoring...");
		        			return;
		        		}
		        		
		        		// build cert
		        		Encoder enc = Base64.getEncoder();
		        		StringBuilder b = new StringBuilder();
		        		try {
		        			b.append("-----BEGIN CERTIFICATE-----").append('\n');
		        			b.append(enc.encodeToString(cert.getEncoded()));
		        			b.append('\n').append("-----END CERTIFICATE-----");
		        		} catch  (Throwable t) {
		        			System.out.println("*** ERROR - unable to encoding certificate for subject name <" + cert.getSubjectDN() + "> - ignoring...");
		        			return;
		        		}
		        		
		        		// write to file
		        		try {
			        		File f = new File(Paths.get(".").toFile(), name + ".pem");
			        		FileWriter fout = new FileWriter(f);
			        		fout.write(b.toString());
			        		fout.flush();
			        		fout.close();
			        		
			        		System.out.println("*** SUCCESS - wrote PEM file for subject name <" + cert.getSubjectDN() + ">");
			        		
		        		} catch (Throwable t) {
		        			System.out.println("*** ERROR - unable to write file for subject name <" + cert.getSubjectDN() + "> - ignoring...");
		        			return;
		        		}
		        		
		        	});
		        }
		    } 
		}; 
		// Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL"); 
	    sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	
		// connect to host
		URL url = new URL(args[0]);
	    HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
		con.setInstanceFollowRedirects(false);
		con.getResponseCode();
	}

}
