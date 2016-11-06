import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

class WebSocketChatClient extends WebSocketClient {

	public WebSocketChatClient( URI serverUri ) {
		super( serverUri );
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		System.out.println( "Connected" );

	}

	@Override
	public void onMessage( String message ) {
		System.out.println( "got: " + message );

	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		System.out.println( "Disconnected" );
		System.exit( 0 );

	}

	@Override
	public void onError( Exception ex ) {
		ex.printStackTrace();

	}

}

public class SSLClientExample {

	/*
	 * Keystore with certificate created like so (in JKS format):
	 *
	 *keytool -genkey -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
	 */
	public static void main( String[] args ) throws Exception {
		WebSocketImpl.DEBUG = true;

		WebSocketChatClient chatclient = new WebSocketChatClient( new URI( "wss://localhost:8887" ) );

		// load up the key store
		String STORETYPE = "JKS";
		String KEYSTORE = "keystore.jks";
		String STOREPASSWORD = "qweasd123";
		String KEYPASSWORD = "qweasd123";

		KeyStore ks = KeyStore.getInstance( STORETYPE );
		File kf = new File( KEYSTORE );
		ks.load( new FileInputStream( kf ), STOREPASSWORD.toCharArray() );

		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
		kmf.init( ks, KEYPASSWORD.toCharArray() );
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
		tmf.init( ks );

		SSLContext sslContext = null;
		sslContext = SSLContext.getInstance( "TLS" );
		sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
		// sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

		SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();

		chatclient.setSocket( factory.createSocket() );

		chatclient.connectBlocking();

		BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String line = reader.readLine();
			if( line.equals( "close" ) ) {
				chatclient.close();
			} else {
				chatclient.send( line );
			}
		}

	}
}
