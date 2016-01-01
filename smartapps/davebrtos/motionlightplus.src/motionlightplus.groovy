/**
 *  MotionLightPlus
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
    name: "MotionLightPlus",
    namespace: "DaveBrtos",
    author: "Dave Brtos",
    description: "Motion Lights Plus",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select Motion Sensor(s) you want to Use") {
        input "motions", "capability.motionSensor", title: "Motion Detectors", required: true, multiple: false
	}
    section("Select Dimmer(s) you want to Use") {
        input "dimmer", "capability.switchLevel", title: "Dimmer Switches", required: true, multiple: false
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
    
    state.MotionCall = false
    state.SwitchLevel = 0 //dimmer?.currentValue("level")?.toInteger()
    //if (state.SwitchLevel == null) state.SwitchLevel = 0
    
    subscribe(dimmer, "switch.on", dimHandler)
    subscribe(dimmer, "switch.off", dimHandler)
    subscribe(dimmer, "switch.setLevel", dimHandler)
    
    subscribe(motions, "motion.active", handleMotionEvent)
    subscribe(motions, "motion.inactive", handleMotionEvent)
}

// TODO: implement event handlers

def dimHandler(evt) {
    log.trace "*********************************"
	log.trace " **** dimHandler ****"
	log.trace "dimmer stat ${evt.value}"
    log.trace "mc ${state.MotionCall}"
    log.trace "so ${state.SwitchLevel}"
    
    def level = 0
    if (evt.value == "on")
    {
        level = 99
    }
    else if (evt.value != "off")
    {
        level = evt.value?.toInteger()
    }
    /*if (state.MotionCall) 
    {
        if (level == 0 && state.SwitchLevel > 0) level = state.SwitchLevel
    }
    else
    {
        state.SwitchLevel = level
    }*/
    if (!state.MotionCall) state.SwitchLevel = level
    state.MotionCall = false
    
    dimmer?.setLevel(level)
    	log.trace "dimmer level ${level}"
log.trace "dimmer stat ${evt.value}"
    log.trace "mc ${state.MotionCall}"
    log.trace "so ${state.SwitchLevel}"
    log.trace "*********************************"
}

def handleMotionEvent(evt) {
    log.trace "*********************************"
	log.trace " **** handleMotionEvent ****"
	log.trace "handleMotionEvent(${evt.value}) Motion detected . . . ."
    //state.MotionCall = true //(state.SwitchLevel == 0)
    log.trace "mc ${state.MotionCall}"
    log.trace "so ${state.SwitchLevel}"
    /*if (state.MotionCall) 
    {
        def level = 0
        if (evt.value == "active") level = 99
        dimmer.setLevel(level)
    }*/
    def level = state.SwitchLevel
    if (evt.value == "active") level = 99
    def curLevel = dimmer?.currentValue("level")?.toInteger()
    state.MotionCall = (level != curLevel)
    dimmer.setLevel(level)
     log.trace "mc ${state.MotionCall}"
    log.trace "so ${state.SwitchLevel}"
    log.trace "*********************************"
}




