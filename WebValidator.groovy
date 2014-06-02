@Grapes([
        @Grab("org.gebish:geb-core:0.9.2"),
        @Grab("org.seleniumhq.selenium:selenium-chrome-driver:2.41.0"),
        @Grab("org.seleniumhq.selenium:selenium-support:2.41.0")
])

import geb.Browser
import geb.Page
import geb.PageChangeListener
import groovy.json.*


class WebValidator
{
    private Long lastcheckpoint = 0
    private Closure testcase

    def port = 1234

    DatagramSocket clientSocket
    InetAddress IPAddress = InetAddress.getByName("239.0.99.114")
    byte[] sendData = new byte[1024]

    public WebValidator(Script s, Closure testcase)
    {
        println s
        println s.args
        this.clientSocket = new DatagramSocket()
	    Browser.metaClass.checkpoint << { String name, Boolean successful ->
		    checkpoint(name,successful)
	    }

        exec(testcase)
    }

    void checkpoint(String name, Boolean successful) {
        def elapsed = System.currentTimeMillis() - lastcheckpoint

        if (successful)
        {
            def dto = new MeterDTO(name, successful, elapsed, System.currentTimeMillis())

            sendPacket dto.asJson()
        }
        else
        {
            throw new Exception("ERROR: ${name} msec " + elapsed)
        }

        setLastCheckpoint()
    }

    void setLastCheckpoint()
    {
        this.lastcheckpoint = System.currentTimeMillis()
    }

    void exec(Closure c) {
        this.lastcheckpoint = System.currentTimeMillis()

        Browser.drive (c)

        System.exit(0)
    }

    void sendPacket(String msg)
    {
        println "Sending packet to " + IPAddress
        println msg
        sendData = msg.getBytes()
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 1234)
        clientSocket.send(sendPacket)
    }

}


class MeterDTO {
    String description
    Boolean success
    Long elapsed
    Long time

    public MeterDTO(String description, Boolean success, Long elapsed, Long time)
    {
        this.description = description
        this.success = success
        this.elapsed = elapsed
        this.time = time
    }

    String asJson()
    {
        return new JsonBuilder(this).toPrettyString()
    }
}
