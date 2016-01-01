/**
 *  AlexaChangeMode
 *
 *  Copyright 2015 Dave Brtos
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
    name: "Alexa Change Mode",
    namespace: "DaveBrtos",
    author: "Dave Brtos",
    description: "AlexaChangeMode",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page name:"pageSetup1"
	page name:"pageSetup2"
	page name:"pageSetup3"
}

def pageSetup1() {
	dynamicPage(name: "pageSetup1", nextPage: "pageSetup2", uninstall: true)
    {        
    	def curMode = location.currentMode
    	section("Choose an Alexa switch to use...") {
    		input "AlexaSwitch", "capability.switch", title: "Alexa Switch", multiple: false, required: true
    	}
    }
}
def pageSetup2() {
	dynamicPage(name: "pageSetup2", nextPage: "pageSetup3", uninstall: true)
    {        
    	section ("When switch is on..."){
        	def phrases = location.helloHome?.getPhrases()*.label
    		if (phrases) {
       			phrases.sort()
			}
       		if (phrases) {
           		input "onPhrase", "enum", title: "Perform this routine", options: phrases, required: false
	        }
    	   	input "onMode", "mode", title: "Change to this mode", required: false
        	input "onSHM", "enum",title: "Change Smart Home Monitor to...", options: ["away":"Arm(Away)", "stay":"Arm(Stay)", "off":"Disarm"], required: false
		}
	}
}
def pageSetup3() {
	dynamicPage(name: "pageSetup3", install: true, uninstall: true)
    {        
    	section ("When switch is off..."){
			def phrases = location.helloHome?.getPhrases()*.label
	    	if (phrases) {
    	   		phrases.sort()
			}
			if (phrases) {
           		input "offPhrase", "enum", title: "Perform this routine", options: phrases, required: false
	        }
    	   	input "offMode", "mode", title: "Change to this mode", required: false
        	input "offSHM", "enum",title: "Change Smart Home Monitor to...", options: ["away":"Arm(Away)", "stay":"Arm(Stay)", "off":"Disarm"], required: false
		}
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
	if (AlexaSwitch){
    	subscribe(AlexaSwitch, "switch", "switchHandler")	
	}
}

//Handlers----------------
def switchHandler(evt) {
	if (evt.value == "on"){
    	turnOn()
    }
    else if (evt.value == "off"){
    	turnOff()
    }
}

def turnOn(){
	if (onPhrase){
		location.helloHome.execute(onPhrase)
	}
	if (onMode) {
		changeMode(onMode)
	}
    if (onSHM){
    	log.debug "Setting Smart Home Monitor to ${onSHM}"
        sendLocationEvent(name: "alarmSystemStatus", value: "${onSHM}")
    }
}

def turnOff(){
	if (offPhrase){
		location.helloHome.execute(offPhrase)
	}
	if (offMode) {
		changeMode(offMode)
	}
    if (offSHM){
    	log.debug "Setting Smart Home Monitor to ${offSHM}"
        sendLocationEvent(name: "alarmSystemStatus", value: "${offSHM}")
    }
}

//Common Methods-------------

def changeMode(newMode) {
	if (location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
		} else {
			log.debug "Unable to change to undefined mode '${newMode}'"
		}
	}
}
