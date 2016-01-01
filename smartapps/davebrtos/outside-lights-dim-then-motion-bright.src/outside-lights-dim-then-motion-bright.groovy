/**
 *  Outside lights dim then motion bright
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
    name: "Outside lights dim then motion bright",
    namespace: "DaveBrtos",
    author: "Dave Brtos",
    description: "Turns on outside lights at dusk at specified brightness,\r\nwhen motion is detected goes to full brightness,\r\ngoes back to specified brightness when motion stops.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Select Motion Sensor(s) you want to Use") {
        input "motions", "capability.motionSensor", title: "Motion Detectors", required: true, multiple: true
	}
    section("Select Dimmer(s) you want to Use") {
        input "switches", "capability.switchLevel", title: "Dimmer Switches", required: true, multiple: true
	}
    section ("Set Brightness Levels") {
		input "DimLevelStr", "enum", title: "Normal Level %", required: true, 
        	options: ["10", "25", "50", "75"], defaultValue: "25"
        
        input "BrightLevelStr", "enum", title: "Bright Level %", required: true, 
        	options: ["100", "75", "50"], defaultValue: "100"
        
        input "DelayMinStr", "enum", title: "Back to Normal Delay (minutes)", required: true, 
        	options: ["1", "3", "5", "10", "15", "30", "60"], defaultValue: "5"
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
	// TODO: subscribe to attributes, devices, locations, etc.
    state.Level = 0
    state.DimLevel = DimLevelStr as Integer
    if (state.DimLevel == 100) {
    	state.DimLevel = 99
    }
    state.BrightLevel = BrightLevelStr as Integer
   	if (state.BrightLevel == 100) {
    	state.BrightLevel = 99
    }
    state.DelayMin = DelayMinStr as Integer
    
	def now = new Date()
    def s = getSunriseAndSunset()

    if(s.sunset.before(now) || s.sunrise.after(now)) {   //before midnight/after sunset or after midnight/before sunset
	  	log.info "Sun is already down"
        runIn(1, sunsetHandler)
    }
    else {
        runIn(1, sunriseHandler)
    }
    
    subscribe(location, "sunset", sunsetHandler)
    subscribe(location, "sunrise", sunriseHandler)
    
    subscribe(motions, "motion.active", handleMotionEvent)
    subscribe(motions, "motion.inactive", handleEndMotionEvent)
}

// TODO: implement event handlers

def handleMotionEvent(evt) {
	log.debug "handleMotionEvent() Motion detected . . . ."
    unschedule(modeDim)   //in case motion is sensed during delay before scheduled modeDim() call
    modeBright()
}

def handleEndMotionEvent(evt) {
	log.debug "handleEndMotionEvent() Motion stopped . . . ."
    runIn((state.DelayMin*60), modeDim)  //delay is number of minutes entered in preferences x 60 to get seconds
                                         //will be unscheduled if it gets light in the meantime
}

def modeOff() {   
	log.debug "modeOff()  Set lights to off state"     
    switches?.setLevel(0)
    state.Level = 0
}

def modeDim() {   
	log.debug "modeDim()  Set lights to dimmed state $state.DimLevel"     
    switches?.setLevel(state.DimLevel)
    state.Level = state.DimLevel
}

def modeBright() {   
	log.debug "modeBright()  Set lights to bright state $state.BrightLevel"
    if (state.Level == state.DimLevel)
    {
        switches?.setLevel(state.BrightLevel)
        state.Level = state.BrightLevel
    }
}

def sunsetHandler(evt) {
    log.debug "Sun has set!"
    modeDim()
}

def sunriseHandler(evt) {
    log.debug "Sun has risen!"
    modeOff()
}