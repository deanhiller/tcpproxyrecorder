tcpproxyrecorder
================

With this library, you can wrap a QA server with this proxy and record everything going into and out of the server.  Once recorded, you can then play it back and verify the behavior as well.

CAVEAT: If your server has a threadpool like a JDBC threadpool, configure that threadpool to just one thread!!!!  This library could be changed to work with a full thread pool but this has not been done and it is sometimes easier just to reconfigure the server to a size of one for the thread pool instead.

You do NEED to feed in a PacketDemarcator to the api so that we know when packetes begin and end otherwise different runs would vary and would not verify correctly and we need everything to be deterministic.
