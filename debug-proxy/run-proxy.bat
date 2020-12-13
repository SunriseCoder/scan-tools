Rem JVM options: java -Djavax.net.ssl.keyStore=yourKEYSTORE -Djavax.net.ssl.keyStorePassword=yourPASSWORD -Djavax.net.debug=ssl

java ^
	-Djavax.net.ssl.keyStore=keystore ^
	-Djavax.net.ssl.keyStorePassword=123456 ^
	-Djavax.net.debug=ssl ^
	-jar target/debug-proxy-0.0.1-SNAPSHOT-jar-with-dependencies.jar 1515
