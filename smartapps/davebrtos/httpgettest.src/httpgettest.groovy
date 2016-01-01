/**
 *  httpGetTest
 *
 *  Copyright 2016 Dave Brtos
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "httpGetTest",
    namespace: "DaveBrtos",
    author: "Dave Brtos",
    description: "httpGetTest",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Server Info") {
    	input "ipAddr", "text", title: "IP Address", defaultValue: "98.228.151.51", required: true
    	input "ipPort", "text", title: "TCP Port", defaultValue: "80", required: true
    }
    section("Speech") {
    	input "words", "text", title: "Message text", required: true
    }
    section("Trigger") {
    	input "aSwitch", "capability.switch", title: "Switch", multiple: false, required: true
    }

}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	if (aSwitch){
    	subscribe(aSwitch, "switch", "switchHandler")
    }
}

def switchHandler(evt) {
	if (evt.value == "on"){
		def params = [
    		uri: "http://" + ipAddr + ":" + ipPort,
   			path: "/" + aSwitch.name
		]


    		httpGet(params) 
}  
}
