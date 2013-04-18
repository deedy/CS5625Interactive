package cs5625.deferred.loop;

import cs5625.deferred.datastruct.EdgeDS;
import cs5625.deferred.datastruct.EdgeData;
import cs5625.deferred.datastruct.EdgeVertexPair;
import cs5625.deferred.datastruct.PolygonData;
import cs5625.deferred.datastruct.VertexAttributeData;
import cs5625.deferred.datastruct.VertexData;
import cs5625.deferred.scenegraph.Mesh;



import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import cs5625.deferred.misc.*;
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
		//TreeMap of new vertices
		TreeMap<Integer, VertexAttributeData> newVerts = new TreeMap<Integer, VertexAttributeData>();
		int newVertID = 0;
		//Scan through all edges and create new vertices with correct positions
		for(Integer edgeID : edgeDS.getEdgeIDs()) {
			EdgeData edge = edgeDS.getEdgeData(edgeID);
			ArrayList<Integer> leftEdges = edgeDS.getOtherEdgesOfLeftFace(edgeID);
			ArrayList<Integer> rightEdges = edgeDS.getOtherEdgesOfRightFace(edgeID);
			VertexAttributeData vert0 = edgeDS.getVertexData(edge.getVertex0()).mData;
			VertexAttributeData vert1 = edgeDS.getVertexData(edge.getVertex1()).mData;
			VertexAttributeData leftVert = null;
			VertexAttributeData rightVert = null;
			//Finding vertex 1 to interpolate with
			if (leftEdges.size() > 0) {
				EdgeData leftEdge = edgeDS.getEdgeData(leftEdges.get(0));
				if (leftEdge.getVertex0() != edge.getVertex0())
					leftVert = edgeDS.getVertexData(leftEdge.getVertex0()).mData;
				else
					leftVert = edgeDS.getVertexData(leftEdge.getVertex1()).mData;
			}
			//Finding vertex 2 to interpolate with
			if (rightEdges.size() > 0) {
				EdgeData rightEdge = edgeDS.getEdgeData(rightEdges.get(0));
				if (rightEdge.getVertex0() != edge.getVertex0())
					rightVert = edgeDS.getVertexData(rightEdge.getVertex0()).mData;
				else
					rightVert = edgeDS.getVertexData(rightEdge.getVertex1()).mData;
			}
			Point3f pos;
			Point2f tex;
			if (leftVert != null && rightVert != null) {
				//Normal Case
				pos = new Point3f(vert0.getPosition());
				tex = new Point2f(vert0.getTexCoord());
				pos.scale(3f/8f);
				tex.scale(3f/8f);
				Point3f temp = new Point3f(vert1.getPosition());
				Point2f temp1 = new Point2f(vert1.getTexCoord());
				temp.scale(3f/8f);
				temp1.scale(3f/8f);
				pos.add(temp);
				tex.add(temp1);
				temp = new Point3f(leftVert.getPosition());
				temp1 = new Point2f(leftVert.getTexCoord());
				temp.scale(1f/8f);
				temp1.scale(1f/8f);
				pos.add(temp);
				tex.add(temp1);
				temp = new Point3f(rightVert.getPosition());
				temp1 = new Point2f(rightVert.getTexCoord());
				temp.scale(1f/8f);
				temp1.scale(1f/8f);
				pos.add(temp);
				tex.add(temp1);
			}
			else { 
				//Boundary Case
				pos = new Point3f(vert0.getPosition());
				tex = new Point2f(vert0.getTexCoord());
				pos.scale(.5f);
				tex.scale(.5f);
				Point3f temp = new Point3f(vert1.getPosition());
				Point2f temp1 = new Point2f(vert1.getTexCoord());
				temp.scale(.5f);
				temp1.scale(.5f);
				pos.add(temp);
				tex.add(temp1);
			}
			newVerts.put(newVertID, new VertexAttributeData(pos, tex)); //Inserting vertex into our TreeMap
			edge.setVertexIDNew(newVertID++); //Storing the new vertex in the edge it was created in
		}
		
		
		//Scan through all vertices to find new positions of the existing ones
		for (Integer vertID : edgeDS.getVertexIDs()) {
			VertexData vert = edgeDS.getVertexData(vertID);
			int adjacency = vert.getConnectedVertices().size();
			Point3f pos = new Point3f(vert.mData.getPosition());
			Point2f tex = new Point2f(vert.mData.getTexCoord());
			float scalar;
			if (adjacency == 2) { 
				// 2 Adjacent Vertices
				scalar = 1f/8f;
			}
			else if (adjacency == 3) { 
				// 3 Adjacent Vertices
				scalar = 3f/16f;
			}
			else { 
				// Warren's rule for more than 3 adjacent vertices
				scalar = 3f / (8f * (float)adjacency);
			}
			pos.scale(1f-(scalar*(float)adjacency));
			tex.scale(1f-(scalar*(float)adjacency));		
			//Scale adjacent vertices appropriately
			for(Integer vertIDnew : vert.getConnectedVertices()) {
				VertexAttributeData vData = edgeDS.getVertexData(vertIDnew).mData;
				Point3f temp = new Point3f(vData.getPosition());
				Point2f temp1 = new Point2f(vData.getTexCoord());
				temp.scale(scalar);
				temp1.scale(scalar);
				pos.add(temp);
				tex.add(temp1);
			}
			newVerts.put(newVertID, new VertexAttributeData(pos, tex)); //Put new positions of even vertices in our TreeMap
			vert.setNewVertexID(newVertID++); //Setting the old vertex IDs to the new ones we created
		}
		
		//Creating a TreeMap for our new edges
		TreeMap<Integer, EdgeData> newEdges = new TreeMap<Integer, EdgeData>();
		int newEdgeID = 0;
		//Creating a TreeMap for our new polygons
		TreeMap<Integer, PolygonData> newPolys = new TreeMap<Integer, PolygonData>();
		int newPolyID = 0;
		//Scan through all the preexisting edges to create new ones
		for(Integer edgeID : edgeDS.getEdgeIDs()) {
			EdgeData edge = edgeDS.getEdgeData(edgeID);
			int newVertex = edge.getNewVertexID();
			int vert0 = edge.getVertex0();
			int vert1 = edge.getVertex1();
			int edge0n = newEdgeID++;
			newEdges.put(edge0n, new EdgeData(vert0,newVertex)); //For each edge put an edge between the one old vertex and the new one
			int edgen1 = newEdgeID++;
			newEdges.put(edgen1, new EdgeData(newVertex,vert1)); // and  an edge between the new one and the other old vertex
			//Store connectivity
			edgeDS.getVertexData(vert0).addVertexConnectivity(new EdgeVertexPair(edge0n, newVertex));
			edgeDS.getVertexData(newVertex).addVertexConnectivity(new EdgeVertexPair(edge0n, vert0));
			edgeDS.getVertexData(newVertex).addVertexConnectivity(new EdgeVertexPair(edgen1, vert1));
			edgeDS.getVertexData(vert1).addVertexConnectivity(new EdgeVertexPair(edgen1, newVertex));
		}
		
		//Scan through all the preexisting polygonal faces to create new ones and the remaining edges
		for(Integer polyID : edgeDS.getPolygonIDs()) {
			PolygonData poly = edgeDS.getPolygonData(polyID);
			ArrayList<Integer> allEdges = poly.getAllEdges();
			//Find the new vertices of the face
			int newvert0 = edgeDS.getEdgeData(allEdges.get(0)).getNewVertexID();
			int newvert1 = edgeDS.getEdgeData(allEdges.get(1)).getNewVertexID();
			int newvert2 = edgeDS.getEdgeData(allEdges.get(2)).getNewVertexID();
			//Insert the 3 new edges between the new vertices
			int edgenv0nv1 = newEdgeID++;
			newEdges.put(edgenv0nv1, new EdgeData(newvert0,newvert1));
			int edgenv1nv2 = newEdgeID++;
			newEdges.put(edgenv1nv2, new EdgeData(newvert1,newvert2));
			int edgenv2nv0 = newEdgeID++;
			newEdges.put(edgenv2nv0, new EdgeData(newvert2,newvert0));
			//Get the 3 preexisting vertices in the triangle
			int vert0 = edgeDS.getEdgeData(allEdges.get(0)).getVertex0();
			int vert1 = edgeDS.getEdgeData(allEdges.get(1)).getVertex0();
			int vert2 = edgeDS.getEdgeData(allEdges.get(2)).getVertex0();
			//Get the 6 edges connecting the 3 preexisting vertices to the new ones
			int edge0nv0 = edgeDS.getVertexData(vert0).getEdgeIDforEdgeWithThisVertex(newvert0).getData();
			int edgenv01 = edgeDS.getVertexData(vert1).getEdgeIDforEdgeWithThisVertex(newvert0).getData();
			int edge1nv1 = edgeDS.getVertexData(vert1).getEdgeIDforEdgeWithThisVertex(newvert1).getData();
			int edgenv12 = edgeDS.getVertexData(vert2).getEdgeIDforEdgeWithThisVertex(newvert1).getData();
			int edge2nv2 = edgeDS.getVertexData(vert2).getEdgeIDforEdgeWithThisVertex(newvert2).getData();
			int edgenv20 = edgeDS.getVertexData(vert0).getEdgeIDforEdgeWithThisVertex(newvert2).getData();
			//Create the 4 polygons of a triangular face accordingly
			newPolys.put(newPolyID++, new PolygonData(edgenv0nv1,edgenv1nv2,edgenv2nv0,newvert0,newvert1,newvert2));
			newPolys.put(newPolyID++, new PolygonData(edge0nv0,edgenv2nv0,edgenv20,vert0,newvert0,newvert2));
			newPolys.put(newPolyID++, new PolygonData(edgenv01,edge1nv1,edgenv0nv1,newvert0,vert1,newvert1));
			newPolys.put(newPolyID++, new PolygonData(edgenv1nv2,edgenv12,edge2nv2,newvert2,newvert1,vert2));
			
		}
		
		// Create the Trimesh

		//this.mMesh = tri.getMesh();
		
		
	}
	
	public Mesh getNewMesh()
	{
		return this.mMesh;
	}
	
}