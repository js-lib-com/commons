package com.jslib.lang;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Default trust manager accept all client and server certificates. It is primarily used to initialize a SSL context for
 * secure HTTP connection.
 * <p>
 * In sample below secure context is initialized before creating HTTPS connection. It uses default trust manager to
 * accept server certificate.
 * 
 * <pre>
 * SSLContext sslContext = SSLContext.getInstance(&quot;TLS&quot;);
 * sslContext.init(new KeyManager[0], new TrustManager[]
 * {
 *   new DefaultTrustManager()
 * }, new SecureRandom());
 * SSLContext.setDefault(sslContext);
 * ...
 * HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
 * </pre>
 * 
 * <b>Warning:</b> use default trust manager only URL is from safe source, perhaps hard coded, and target server is
 * safe.
 * 
 * @author Iulian Rotaru
 */
public class DefaultTrustManager implements X509TrustManager
{
  /**
   * Given the partial or complete certificate chain provided by the peer, build a certificate path to a trusted root
   * and return if it can be validated and is trusted for client SSL authentication based on the authentication type.
   * 
   * @param chain the peer certificate chain
   * @param authType the authentication type based on the client certificate
   * @throws IllegalArgumentException if null or zero-length chain is passed in for the chain parameter or if null or
   *           zero-length string is passed in for the authType parameter.
   * @throws CertificateException if the certificate chain is not trusted by this TrustManager.
   */
  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
  {
  }

  /**
   * Given the partial or complete certificate chain provided by the peer, build a certificate path to a trusted root
   * and return if it can be validated and is trusted for server SSL authentication based on the authentication type.
   * 
   * @param chain the peer certificate chain
   * @param authType the authentication type based on the client certificate
   * @throws IllegalArgumentException if null or zero-length chain is passed in for the chain parameter or if null or
   *           zero-length string is passed in for the authType parameter.
   * @throws CertificateException if the certificate chain is not trusted by this TrustManager.
   */
  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
  {
  }

  /**
   * Return an array of certificate authority certificates which are trusted for authenticating peers.
   * 
   * @return a non-null (possibly empty) array of acceptable CA issuer certificates.
   */
  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    return null;
  }
}
