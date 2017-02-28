/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package backend.verification.platu.project;

/**
 *
 * @author ldmtwo
 */
public class PrintStackTrace extends Exception{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PrintStackTrace() {
        this.printStackTrace();
    }

    public PrintStackTrace(String s) {
        super(s);
        this.printStackTrace();
    }
}
