# RiakBackedCAS

## Setting up Riak
To install *riakbackedcas* you'll need a linux OS running Riak. These notes are based on Ubuntu (ubuntu-12.04-server-amd64) with Riak 1.4. Installing Riak was easily done following the installation instructions on the [Basho site](http://docs.basho.com/riak/latest/ops/building/installing/debian-ubuntu/#Installing-From-Apt-Get). 

RiakBackedCAS will make a Riak bucket, imaginatively named *cas*. To look inside buckets Riak have an [HTTP API](http://docs.basho.com/riak/latest/dev/references/http/) that will get you started with querying Riak. I mostly used these commands.

    $ curl -v http://localhost:8098/buckets?buckets=true
    $ curl -v http://localhost:8098/buckets/cas/keys?keys=true

Once CAS is running check that you have a *catalina.out* with entries like

    2014-03-11 03:24:20,634 INFO [uk.co.nascency.riakbackedcas.registry.RiakBackedTicketRegistry] - <Connecting to RIAK>

## Adding some CAS
Now let's get CAS running. Assuming that you have already cloned then the next step is to check your Java version before attempting to build the code. My environment as described by Maven is:

    $ mvn --version
    Apache Maven 3.0.4
    Maven home: /usr/share/maven
    Java version: 1.7.0_51, vendor: Oracle Corporation
    Java home: /usr/lib/jvm/java-7-openjdk-amd64/jre
    Default locale: en_GB, platform encoding: UTF-8
    OS name: "linux", version: "3.2.0-23-generic", arch: "amd64", family: "unix"

From here, it should be possible to walk-through the Maven build-cycle until you get to BUILD SUCCESSFUL and then you will find some new war files.

    ./cas-overlay-server-demo/target/cas-server.war
    ./cas-overlay-management-demo/target/cas-overlay-management.war

Move the war files into */var/lib/tomcat6/webapps*

Check that SSL is enabled in */var/lib/tomcat6/conf/server.xml*
Usually this is just a matter of uncommenting the following lines.

      <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
               maxThreads="150" scheme="https" secure="true"
               clientAuth="false" sslProtocol="TLS" />

You will also need a certificate in your key store. In my case this was done using the following commands and the traditional password of *changeit*.

    $ keytool -genkey -alias tomcat -keyalg RSA
    $ keytool -list -keystore /usr/share/tomcat6/.keystore

Having restarted tomcat you can attempt a login by pointing your browser towards something similar to:

**https://cas.local:8443/cas-server/**

The hostname will (obviously) depend on your local environment. RiakBackedCAS is authenticating using the SimpleTestAuthenticationHandler provided by CAS. Logging in with username *dog* and password *dog* works but *dog* and *cat* doesn't. Anything works as long the username matches the password. CAS version is *4.0.0-RC4*.
