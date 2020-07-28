# Link State Routing Protocol
User-space program which simulates some of the major functionalities of a routing device running a simplified Link State Routing protocol 

# How to Execute:

To execute:
On different command lines to simmulate different computers:

mvn compile exec:java -Dexec.mainClass=socs.network.Main -Dexec.args="conf/router1.conf"

mvn compile exec:java -Dexec.mainClass=socs.network.Main -Dexec.args="conf/router2.conf"

mvn compile exec:java -Dexec.mainClass=socs.network.Main -Dexec.args="conf/router3.conf"

mvn compile exec:java -Dexec.mainClass=socs.network.Main -Dexec.args="conf/router4.conf"

mvn compile exec:java -Dexec.mainClass=socs.network.Main -Dexec.args="conf/router5.conf"

mvn compile exec:java -Dexec.mainClass=socs.network.Main -Dexec.args="conf/router6.conf"

# To attach routers

On the command line:
attach conf/router1.conf 3
//Can be replaces with any other router available, don't forget to add a weight

start
//Starts the link between the routers

# To check on neighbors

On the command line:
neighbors

# To detect the path of routers (using shortest/less weighted path):

On the command line, depending on the router that's attached in the network:
detect 192.168.1.1

detect 192.168.1.2

detect 192.168.1.3

detect 192.168.3.1

detect 192.168.1.5
