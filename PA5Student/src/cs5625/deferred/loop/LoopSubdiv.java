package cs5625.deferred.loop;

import cs5625.deferred.datastruct.EdgeDS;
import cs5625.deferred.datastruct.EdgeData;
import cs5625.deferred.datastruct.EdgeVertexPair;
import cs5625.deferred.datastruct.PolygonData;
import cs5625.deferred.datastruct.VertexAttributeData;
import cs5625.deferred.datastruct.VertexData;
import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.Trimesh;



import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
		// REMOVE
//		Set<Integer> vids = edgeDS.getVertexIDs();
//		for(int id : vids){
//			System.out.println(id+", " + edgeDS.getVertexData(id).mData.getPosition());
//		}
//		System.out.println("=====================");
		
		
		
		// Maps for new verts, edges, polys
		TreeMap<Integer, VertexData> newVerts = new TreeMap<Integer, VertexData>();
		int newVertID = 0;
	
		
		TreeMap<Integer, EdgeData> newEdges = new TreeMap<Integer, EdgeData>();
		int newEdgeID = 0;
		TreeMap<Integer, PolygonData> newPolys = new TreeMap<Integer, PolygonData>();
		int newPolyID =0;
		
		
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
				// Normal Case
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
			
			// REMOVE
			//System.out.println(newVertID+", "+ pos);
			
			 //Inserting vertex into our TreeMap
			VertexData vertdata = new VertexData(new VertexAttributeData(pos,tex));
			newVerts.put(newVertID, vertdata); //Put new positions of even vertices in our TreeMap
			
			//Storing the new vertex in the edge it was created in
			edge.setVertexIDNew(newVertID++); 
			
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
			VertexData vertdata = new VertexData(new VertexAttributeData(pos,tex));
			newVerts.put(newVertID, vertdata); //Put new positions of even vertices in our TreeMap
			vert.setNewVertexID(newVertID++); //Setting the old vertex IDs to the new ones we created
		}
		
//		for (Integer a : newVerts.keySet()) {
//			VertexAttributeData x = newVerts.get(a).mData;
//			System.out.println(a+" - "+x.getPosition());
//		}
		for (Integer a : edgeDS.getVertexIDs()) {
			VertexData vert = edgeDS.getVertexData(a);
			System.out.println(vert.getNewVertexID()+" - "+a+" - "+vert.mData.getPosition());
		}
			

		//Scan through all the pre-existing edges to create new ones
		for(Integer edgeID : edgeDS.getEdgeIDs()) {
			
			EdgeData edge = edgeDS.getEdgeData(edgeID);
			int newVertex = edge.getNewVertexID();
			
			int vert0 = edgeDS.getVertexData(edge.getVertex0()).getNewVertexID();
			int vert1 = edgeDS.getVertexData(edge.getVertex1()).getNewVertexID();
			
			int edge0n = newEdgeID++;
			int edgen1 = newEdgeID++;
			
			newEdges.put(edge0n, new EdgeData(vert0,newVertex)); //For each edge put an edge between the one old vertex and the new one			
			newEdges.put(edgen1, new EdgeData(newVertex,vert1)); // and  an edge between the new one and the other old vertex
			
			//Store connectivity (in pairs)
			newVerts.get(vert0).addVertexConnectivity(new EdgeVertexPair(edge0n, newVertex));
			newVerts.get(newVertex).addVertexConnectivity(new EdgeVertexPair(edge0n, vert0));
			newVerts.get(newVertex).addVertexConnectivity(new EdgeVertexPair(edgen1, vert1));
			newVerts.get(vert1).addVertexConnectivity(new EdgeVertexPair(edgen1, newVertex));	
		}
		

		
		//Scan through all the pre-existing polygons to create new ones and the remaining edges
		for(Integer polyID : edgeDS.getPolygonIDs()) {
			PolygonData poly = edgeDS.getPolygonData(polyID);
			ArrayList<Integer> allEdges = poly.getAllEdges();
			
			//Find the new vertices of the face
			// XXX RESUME - remove need for edgeDS - causing errors because it contains invalid data.
			int newvert0 = edgeDS.getEdgeData(allEdges.get(0)).getNewVertexID();
			int newvert1 = edgeDS.getEdgeData(allEdges.get(1)).getNewVertexID();
			int newvert2 = edgeDS.getEdgeData(allEdges.get(2)).getNewVertexID();
			
			//Insert the 3 new edges between the new vertices
			int edgenv0nv1 = newEdgeID++;
			int edgenv1nv2 = newEdgeID++;
			int edgenv2nv0 = newEdgeID++;
			
			newEdges.put(edgenv0nv1, new EdgeData(newvert0,newvert1));			
			newEdges.put(edgenv1nv2, new EdgeData(newvert1,newvert2));			
			newEdges.put(edgenv2nv0, new EdgeData(newvert2,newvert0));
			
			//Get the 3 pre-existing vertices in the triangle
			int vert0 = edgeDS.getVertexData(poly.getAllVertices().get(0)).getNewVertexID();
			int vert1 = edgeDS.getVertexData(poly.getAllVertices().get(1)).getNewVertexID();
			int vert2 = edgeDS.getVertexData(poly.getAllVertices().get(2)).getNewVertexID();
//			System.out.println(vert0+" "+vert1+" "+vert2);
//			
//			System.out.println("Face"+polyID+" edges: " + allEdges);
//			
//			System.out.println("Face" +polyID+" verts: " + poly.getAllVertices());
//			System.out.println("verts: " + vert0 +","+vert1+","+vert2);
//			System.out.println("intermediate verts: " + newvert0 +","+newvert1+","+newvert2);
//			
			//Get the 6 edges connecting the 3 pre-existing vertices to the new ones
			//System.out.println("CANT FIND EDGE WITH: " + vert1 + " , " + newvert0);
			
			int e1 = newVerts.get(vert0).getEdgeIDforEdgeWithThisVertex(newvert0).getData();
			int e2 = newVerts.get(newvert0).getEdgeIDforEdgeWithThisVertex(vert1).getData();
			int e3 = newVerts.get(vert1).getEdgeIDforEdgeWithThisVertex(newvert1).getData();
			int e4 = newVerts.get(newvert1).getEdgeIDforEdgeWithThisVertex(vert2).getData();
			int e5 = newVerts.get(vert2).getEdgeIDforEdgeWithThisVertex(newvert2).getData();
			int e6 = newVerts.get(newvert2).getEdgeIDforEdgeWithThisVertex(vert0).getData();
//			
//			////////////////////////////////////////////////////
//			// Create new edges forming center face of subdivision
//			
//			int e7 = newEdgeID++;
//			newEdges.put(e7, new EdgeData(newvert0,newvert1));
//			newVerts.get(newvert0).addVertexConnectivity(new EdgeVertexPair(e7, newvert1));			
//			newVerts.get(newvert1).addVertexConnectivity(new EdgeVertexPair(e7, newvert0));
//
//			int e8 = newEdgeID++;
//			newEdges.put(e8, new EdgeData(newvert0,newvert1));
//			newVerts.get(newvert0).addVertexConnectivity(new EdgeVertexPair(e8, newvert1));			
//			newVerts.get(newvert1).addVertexConnectivity(new EdgeVertexPair(e8, newvert0));
//			
//			int e9 = newEdgeID++;
//			newEdges.put(e9, new EdgeData(newvert0,newvert1));
//			newVerts.get(newvert0).addVertexConnectivity(new EdgeVertexPair(e9, newvert1));			
//			newVerts.get(newvert1).addVertexConnectivity(new EdgeVertexPair(e9, newvert0));
//			
//			////////////////////////////////////////////////////

			
			//Create the 4 polygons of a triangular face accordingly
			newPolys.put(newPolyID++, new PolygonData(e1,edgenv2nv0,e6,vert0,newvert0,newvert2));
			newPolys.put(newPolyID++, new PolygonData(e2,e3,edgenv0nv1,newvert0,vert1,newvert1));
			newPolys.put(newPolyID++, new PolygonData(edgenv1nv2,e4,e5,newvert2,newvert1,vert2));
			newPolys.put(newPolyID++, new PolygonData(edgenv0nv1,edgenv1nv2,edgenv2nv0,newvert0,newvert1,newvert2));
			
		}
		
		// Create the Trimesh

		//this.mMesh.setVertexData();
		System.out.println("CURRENT STATE DATA: \n ===============");
		for(Integer vertID : newVerts.keySet()) {
			System.out.println("v"+vertID +": " + newVerts.get(vertID).mData.getPosition());
		}
		for(Integer edgeID : newEdges.keySet()) {
			System.out.println("e"+edgeID +": (" + newEdges.get(edgeID).getVertex0()+", " +newEdges.get(edgeID).getVertex1()+")");
		}
		for(Integer faceID : newPolys.keySet()) {
			System.out.println("f"+faceID +": " + newPolys.get(faceID).getAllEdges());
		}
		
		/// TODO ADD VERTS TO MESH
		FloatBuffer vs = FloatBuffer.allocate(3*newVerts.keySet().size());
		FloatBuffer ts = FloatBuffer.allocate(2*newVerts.keySet().size());
		for(Integer v : newVerts.keySet()){
			Point3f p = newVerts.get(v).mData.getPosition();
			Point2f t = newVerts.get(v).mData.getTexCoord(); 
			vs.put(p.x);
			vs.put(p.y);
			vs.put(p.z);
			ts.put(t.x);
			ts.put(t.y);
			//vs.put(newVerts.get(v).mData.getPosition().get(arg0))
		}
		IntBuffer ps = IntBuffer.allocate(3*newPolys.keySet().size());
		for(Integer p : newPolys.keySet()){
			ArrayList<Integer> verts = newPolys.get(p).getAllVertices();
			ps.put(verts.get(0));
			ps.put(verts.get(1));
			ps.put(verts.get(2));
		}
		IntBuffer es = IntBuffer.allocate(2*newEdges.keySet().size());
		for(Integer e : newEdges.keySet()){
			es.put(newEdges.get(e).getVertex0());
			es.put(newEdges.get(e).getVertex1());
		}
		//Trimesh m = new Trimesh();
		this.mMesh = edgeDS.getMesh();
		Trimesh m = (Trimesh) this.mMesh.clone();
		m.setVertexData(vs);
		m.setTexCoordData(ts);
		m.setEdgeData(es);
		m.setPolygonData(ps);
		System.out.println("Polycount: "+m.getPolygonCount()+" "+newPolys.keySet().size());
		System.out.println("Vertexcount: "+m.getVertexCount()+" "+newVerts.keySet().size());
		System.out.println("Edgecount: "+m.getEdgeData().capacity()+" "+newEdges.keySet().size());

		this.mMesh = (Mesh) m;
		
		
		
	}
	
	public Mesh getNewMesh()
	{
		return this.mMesh;
	}
	
}
