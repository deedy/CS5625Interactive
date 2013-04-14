package cs5625.deferred.loop;

import cs5625.deferred.datastruct.EdgeDS;
import cs5625.deferred.scenegraph.Mesh;

/**
 * LoopSubdiv.java
 * 
 * Perform the subdivision in this class/package 
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Rohit Garg (rg534)
 * @date 2012-03-23
 */

public class LoopSubdiv {
	
	private Mesh mMesh;
	
	public LoopSubdiv(EdgeDS edgeDS)
	{
		// TODO PA5: Fill in this function to perform loop subdivision
		this.mMesh = edgeDS.getMesh();
		
		
		
	}
	
	public Mesh getNewMesh()
	{
		return this.mMesh;
	}
	
}
