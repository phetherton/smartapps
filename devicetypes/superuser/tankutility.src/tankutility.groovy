preferences {
        input("token", "string", title:"Authentication Token", description: "Authentication Token", required: true, displayDuringSetup: true)
        input("deviceID", "string", title:"Device ID", description: "Device ID", required: true, displayDuringSetup: true)
        input("email", "string", title:"Email", description: "You@you.com", required: true, displayDuringSetup: true)
}

//https://github.com/phetherton/smartapps/blob/master/devicetypes/superuser/tankutility.src/tank-image.jpg?raw=true


import javax.mail.internet.*;
import javax.mail.*
import javax.activation.*
import groovy.time.TimeCategory
 
metadata {
        // Automatically generated. Make future change here.
        definition (name: "TANKUTILITY", author: "Patrick Hetherton/Zach Armstrong") {
                capability "Energy Meter"
                capability "Polling"
                capability "Refresh"
                capability "Sensor"
                capability "Power Meter"
                capability "Temperature Measurement"
        }
        // UI tile definitions
        tiles (scale: 2){
                valueTile(      "level", "device.level", width: 6, height:4)            {
                        state(  "device.level",label:'${currentValue} %', icon: "https://github.com/phetherton/smartapps/blob/master/devicetypes/superuser/tankutility.src/tank-image.jpg?raw=true",  backgroundColors:[
                        [value: 10, color: "#bc2323"],
                        [value: 20, color: "#bc2323"],
                        [value: 30, color: "#f1d801"],
                        [value: 40, color: "#f1d801"],
                        [value: 50, color: "#f1d801"],
                        [value: 60, color: "#44b621"],
                        [value: 70, color: "#90d2a7"],
                        [value: 80, color: "#1e9cbb"],
                        [value: 90, color: "#153591"]
                        ]
                        )
                }

                valueTile(      "temperature", "device.temperature",width: 6,  height: 1 ) {
                        state("device.temperature", label:'${currentValue} F' )
                }
              
                valueTile(      "SystemHealth", "device.SystemHealth",width: 3,  height: 4) {
                        state("device.SystemHealth", label:'System Health',  backgroundColors:[
                        [value: 1, color: "#bc2323"],
                        [value: 0, color: "#44b621"]
                        ] 
                        )  
                        
                        }
                         
                                             
                valueTile(      "reading", "device.reading",width: 3,  height:  2) {
                        state("device.reading",bacgroundColor: "#00a0dc", label:'${currentValue}                          Last Reading')
                }
              
                valueTile(      "owner", "device.owner",width: 6,  height: 1 ) {
                        state("device.owner", label:'${currentValue}' )
                }
                
                valueTile("refresh", "command.refresh",width: 3,  height: 2) {
                        state "default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
                }
                
               
             
               
                main(["level"])
                details(["temperature","level", "reading", "SystemHealth","refresh"])
                    }             
      }

def poll() {
        log.trace 'Poll Called'
        runCmd()
}

def refresh() {
        log.trace 'Refresh Called'
        runCmd()
}

def runCmd() {

        def params = [
        uri: "https://data.tankutility.com/api/devices/$deviceID?token=$token"
        ]

   try {
       httpGet(params) { resp ->
           log.debug "response data: ${resp.data}"
           log.debug "temperature: ${resp.data.device.lastReading.temperature}"
           log.debug "level: ${resp.data.device.lastReading.tank}"
           log.debug "reading: ${resp.data.device.lastReading.time}"
           log.debug "owner: ${resp.data.device.name}"
                       
           TimeZone timeZone = TimeZone.getTimeZone("America/New_York")
           Date currTime = new Date(resp.data.device.lastReading.time)
           def newreading = 0
           def currTimeString = currTime.format('MM/dd/YY hh:mm a', timeZone) //Format the date to Eastern 
           log.debug "reading to EST: ${currTimeString}"
           def date = new Date() //Get todays date time
           def addDate = currTime + 1 // date arithmetic using the "plus" method
           
           def date2 = date.format('MM/dd/YY hh:mm a', timeZone) // Format the date to Eastern
           log.debug "Todays Date is ${date2} " 
           
           def currTimeStringplus1 = addDate.format('MM/dd/YY hh:mm a', timeZone) // Format the date to Eastern
           log.debug "Todays Date +1 : ${currTimeStringplus1} "
           
           if (addDate < date ) // if the last reading Date + 1 is less than todays date we have not read in 24 hours problems exists 
                         newreading = newreading + 1
           else          newreading = newreading + 0
           
           log.debug "BK Value: ${newreading} "
          
                         
           sendEvent (name: "temperature", value: (resp.data.device.lastReading.temperature).toInteger(), unit:"F")
           sendEvent (name: "level", value: (resp.data.device.lastReading.tank).toInteger(), unit:"%")
           sendEvent (name: "reading", value: (currTimeString), unit: "EST")
           sendEvent (name: "owner", value: (resp.data.device.name))
           sendEvent (name: "SystemHealth", value: (newreading).toInteger(), unit: "%")
           
       }
  
    } catch (e) {
        log.error "something went wrong: $e"
    }

}

def parse(String description) {
        log.debug "Got parse: $description"
        return
}
