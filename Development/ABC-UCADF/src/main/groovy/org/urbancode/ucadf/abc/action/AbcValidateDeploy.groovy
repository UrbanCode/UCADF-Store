package org.urbancode.ucadf.abc.action

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class AbcValidateDeploy extends UcAdfAction {
	// The action properties
	String application
	String environment
	String snapshot
	String requestor
	String changeId
	String compProcessRequestId
	String appProcessRequestId
	
	/**
	 * Runs the action.	
	 * @return Returns true if valid.
	 */
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("This is where specific change control integrations can be done.")
		
		return true
	}
}
