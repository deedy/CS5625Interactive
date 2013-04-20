package cs5625.deferred.catmullclark;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.datastruct.EdgeDS;
import cs5625.deferred.datastruct.EdgeData;
import cs5625.deferred.datastruct.EdgeVertexPair;
import cs5625.deferred.datastruct.PolygonData;
import cs5625.deferred.datastruct.VertexAttributeData;
import cs5625.deferred.datastruct.VertexData;
import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.Quadmesh;
import cs5625.deferred.scenegraph.Trimesh;

public class CCSubdiv {
	
	private Mesh mMesh;
	
	public CCSubdiv(EdgeDS edgeDS)
	{
		// TODO PA5: Fill in this function to perform catmull clark subdivision
		// Maps for new verts, edges, polys
		TreeMap<Integer, VertexData> newVerts = new TreeMap<Integer, VertexData>();
		int newVertID = 0;
		TreeMap<Integer, EdgeData> newEdges = new TreeMap<Integer, EdgeData>();
		int newEdgeID = 0;
		TreeMap<Integer, PolygonData> newPolys = new TreeMap<Integer, PolygonData>();
		int newPolyID = 0;

		
		//  Scan through all polygons and create new vertices at the center of the polygons
		for (Integer polyID : edgeDS.getPolygonIDs()) {
			PolygonData p = edgeDS.getPolygonData(polyID);
			
			// center vertex
			VertexAttributeData p1 = edgeDS.getVertexData(p.getAllVertices().get(0)).mData;
			VertexAttributeData p2 = edgeDS.getVertexData(p.getAllVertices().get(1)).mData;
			VertexAttributeData p3 = edgeDS.getVertexData(p.getAllVertices().get(2)).mData;
			VertexAttributeData p4 = edgeDS.getVertexData(p.getAllVertices().get(3)).mData;
			Point3f pos = new Point3f(p1.getPosition());
			Point2f tex = new Point2f(p1.getTexCoord());
			pos.scale(1f / 4f);
			tex.scale(1f / 4f);
			Point3f temp = new Point3f(p2.getPosition());
			Point2f temp1 = new Point2f(p2.getTexCoord());
			temp.scale(1f / 4f);
			temp1.scale(1f / 4f);
			pos.add(temp);
			tex.add(temp1);
			temp = new Point3f(p3.getPosition());
			temp1 = new Point2f(p3.getTexCoord());
			temp.scale(1f / 4f);
			temp1.scale(1f / 4f);
			pos.add(temp);
			tex.add(temp1);
			temp = new Point3f(p4.getPosition());
			temp1 = new Point2f(p4.getTexCoord());
			temp.scale(1f / 4f);
			temp1.scale(1f / 4f);
			pos.add(temp);
			tex.add(temp1);		
			// Inserting vertex into our TreeMap
			VertexData vertdata = new VertexData(new VertexAttributeData(pos, tex));
			newVerts.put(newVertID, vertdata); // Put new positions of even
												// vertices in our TreeMap

			// Storing the new vertex in the face it was created in			
			p.setNewFaceVertexID(newVertID++);
			
		}
		for (Integer edgeID : edgeDS.getEdgeIDs()) {
			
			EdgeData edge = edgeDS.getEdgeData(edgeID);
			VertexAttributeData p1 = edgeDS.getVertexData(edge.getVertex0()).mData;
			VertexAttributeData p2 = edgeDS.getVertexData(edge.getVertex1()).mData;
			ArrayList<Integer> leftEdges = edgeDS.getOtherEdgesOfLeftFace(edgeID);
			ArrayList<Integer> rightEdges = edgeDS.getOtherEdgesOfRightFace(edgeID);
			Point3f pos;
			Point2f tex;
			if (edgeDS.isCreaseEdge(edgeID) || leftEdges.size()!=3 || rightEdges.size()!=3) {
				pos = new Point3f(p1.getPosition());
				tex = new Point2f(p1.getTexCoord());
				pos.scale(1f / 2f);
				tex.scale(1f / 2f);
				Point3f temp = new Point3f(p2.getPosition());
				Point2f temp1 = new Point2f(p2.getTexCoord());
				temp.scale(1f / 2f);
				temp1.scale(1f / 2f);
				pos.add(temp);
				tex.add(temp1);
			} else {
				PolygonData poly1 = edgeDS.getPolygonData(edge.getPolys().get(0));
				PolygonData poly2 = edgeDS.getPolygonData(edge.getPolys().get(1));
				pos = new Point3f(p1.getPosition());
				tex = new Point2f(p1.getTexCoord());
				pos.scale(3f / 8f);
				tex.scale(3f / 8f);
				Point3f temp = new Point3f(p2.getPosition());
				Point2f temp1 = new Point2f(p2.getTexCoord());
				temp.scale(3f / 8f);
				temp1.scale(3f / 8f);
				pos.add(temp);
				tex.add(temp1);
				for (Integer vertID: poly1.getAllVertices()) {
					if (vertID != edge.getVertex0() && vertID != edge.getVertex1()) {
						temp = new Point3f(edgeDS.getVertexData(vertID).mData.getPosition());
						temp1 = new Point2f(edgeDS.getVertexData(vertID).mData.getTexCoord());
						temp.scale(1f / 16f);
						temp1.scale(1f / 16f);
						pos.add(temp);
						tex.add(temp1);
					}
				}
				for (Integer vertID: poly2.getAllVertices()) {
					if (vertID != edge.getVertex0() && vertID != edge.getVertex1()) {
						temp = new Point3f(edgeDS.getVertexData(vertID).mData.getPosition());
						temp1 = new Point2f(edgeDS.getVertexData(vertID).mData.getTexCoord());
						temp.scale(1f / 16f);
						temp1.scale(1f / 16f);
						pos.add(temp);
						tex.add(temp1);
					}
				}
				
			}
			 //Inserting vertex into our TreeMap
			VertexData vertdata = new VertexData(new VertexAttributeData(pos,tex));
			newVerts.put(newVertID, vertdata); //Put new positions of even vertices in our TreeMap
			
			//Storing the new vertex in the edge it was created in
			edge.setVertexIDNew(newVertID++); 
		}
		// Scan through all vertices to find new positions of the existing ones
		for (Integer vertID : edgeDS.getVertexIDs()) {
			VertexData vert = edgeDS.getVertexData(vertID);
			int creaseCount = 0;
			ArrayList<Integer> creaseVerts = new ArrayList<Integer>();
		
			Point3f pos = new Point3f(vert.mData.getPosition());
			Point2f tex = new Point2f(vert.mData.getTexCoord());
			for (Integer edgeID : vert.getConnectedEdges()) {
				if (edgeDS.isCreaseEdge(edgeID)) {
					creaseCount++;
					EdgeData tmp = edgeDS.getEdgeData(edgeID);
					if (tmp.getVertex0()==vertID) 
						creaseVerts.add(tmp.getVertex1());
					else
						creaseVerts.add(tmp.getVertex0());
				}
			}
			if (creaseCount > 2) {
				
			} else if (creaseCount == 2) {
				pos.scale(3f / 4f);
				tex.scale(3f / 4f);
				Point3f temp = new Point3f(edgeDS.getVertexData(creaseVerts.get(0)).mData.getPosition());
				Point2f temp1 = new Point2f(edgeDS.getVertexData(creaseVerts.get(0)).mData.getTexCoord());
				temp.scale(1f / 8f);
				temp1.scale(1f / 8f);
				pos.add(temp);
				tex.add(temp1);
				temp = new Point3f(edgeDS.getVertexData(creaseVerts.get(1)).mData.getPosition());
				temp1 = new Point2f(edgeDS.getVertexData(creaseVerts.get(1)).mData.getTexCoord());
				temp.scale(1f / 8f);
				temp1.scale(1f / 8f);
				pos.add(temp);
				tex.add(temp1);
			} else if (creaseCount < 2) {
				float n = vert.getValence();
				pos.scale((n-2) / n);
				tex.scale((n-2) / n);
				Point3f temp;
				Point2f temp1;
				ArrayList<Integer> adjacentPolys = new ArrayList<Integer>();
				for (Integer edgeID : vert.getConnectedEdges()) {
					EdgeData tmp = edgeDS.getEdgeData(edgeID);
					//Not sure if adjacent vertices or middle-edge vertices
					if (tmp.getVertex0()==vertID) {
						temp = new Point3f(edgeDS.getVertexData(tmp.getVertex1()).mData.getPosition());
						temp1 = new Point2f(edgeDS.getVertexData(tmp.getVertex1()).mData.getTexCoord());
					} else {
						temp = new Point3f(edgeDS.getVertexData(tmp.getVertex0()).mData.getPosition());
						temp1 = new Point2f(edgeDS.getVertexData(tmp.getVertex0()).mData.getTexCoord());
					}
					temp.scale(1f / (n*n));
					temp1.scale(1f / (n*n));
					pos.add(temp);
					tex.add(temp1);
					for (Integer polyID : tmp.getPolys()) {
						if (!adjacentPolys.contains(polyID)) {
							adjacentPolys.add(polyID);
						}
					}
				}
				for (Integer polyID : adjacentPolys) {
					VertexData f_i = newVerts.get(edgeDS.getPolygonData(polyID).getNewFaceVertexID());
					temp = new Point3f(f_i.mData.getPosition());
					temp1 = new Point2f(f_i.mData.getTexCoord());
					temp.scale(1f / (n*n));
					temp1.scale(1f / (n*n));
					pos.add(temp);
					tex.add(temp1);
				}
			}
			 //Inserting vertex into our TreeMap
			VertexData vertdata = new VertexData(new VertexAttributeData(pos,tex));
			newVerts.put(newVertID, vertdata); //Put new positions of even vertices in our TreeMap
			vert.setNewVertexID(newVertID++);		
		}
		// Scan through all the pre-existing edges to create new ones
		for (Integer edgeID : edgeDS.getEdgeIDs()) {

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

		// Scan through all the pre-existing polygons to create new ones and the remaining edges
		for (Integer polyID : edgeDS.getPolygonIDs()) {
			PolygonData poly = edgeDS.getPolygonData(polyID);
			ArrayList<Integer> allEdges = poly.getAllEdges();
			//Find the new vertices of the face

			int newvert0 = edgeDS.getEdgeData(allEdges.get(0)).getNewVertexID();
			int newvert1 = edgeDS.getEdgeData(allEdges.get(1)).getNewVertexID();
			int newvert2 = edgeDS.getEdgeData(allEdges.get(2)).getNewVertexID();
			int newvert3 = edgeDS.getEdgeData(allEdges.get(3)).getNewVertexID();
			int facevert = poly.getNewFaceVertexID();
			// Insert the 4 edges from the face center to the new edge vertices
			int edgenv0f = newEdgeID++;
			int edgenv1f = newEdgeID++;
			int edgenv2f = newEdgeID++;
			int edgenv3f = newEdgeID++;
			
			newEdges.put(edgenv0f, new EdgeData(newvert0,facevert));			
			newEdges.put(edgenv1f, new EdgeData(newvert1,facevert));			
			newEdges.put(edgenv2f, new EdgeData(newvert2,facevert));
			newEdges.put(edgenv3f, new EdgeData(newvert3,facevert));
			
			//Get the 4 pre-existing vertices in the quad
			int vert0 = edgeDS.getVertexData(poly.getAllVertices().get(0)).getNewVertexID();
			int vert1 = edgeDS.getVertexData(poly.getAllVertices().get(1)).getNewVertexID();
			int vert2 = edgeDS.getVertexData(poly.getAllVertices().get(2)).getNewVertexID();
			int vert3 = edgeDS.getVertexData(poly.getAllVertices().get(3)).getNewVertexID();


			// Create the 4 new quads accordingly
			newPolys.put(newPolyID++, new PolygonData(0,0,0,0,newvert3,facevert,newvert0,vert0));
			newPolys.put(newPolyID++, new PolygonData(0,0,0,0,newvert2,facevert,newvert3,vert3));
			newPolys.put(newPolyID++, new PolygonData(0,0,0,0,newvert1,facevert,newvert2,vert2));
			newPolys.put(newPolyID++, new PolygonData(0,0,0,0,newvert0,facevert,newvert1,vert1));
		}
		

		// Create the quadmesh
		FloatBuffer vs = Buffers.newDirectFloatBuffer(3 * newVerts.keySet().size());
		FloatBuffer ts = Buffers.newDirectFloatBuffer(2 * newVerts.keySet().size());
		for (Integer v : newVerts.keySet()) {
			Point3f p = newVerts.get(v).mData.getPosition();
			Point2f t = newVerts.get(v).mData.getTexCoord();
			vs.put(p.x);
			vs.put(p.y);
			vs.put(p.z);
			ts.put(t.x);
			ts.put(t.y);
		}
		vs.rewind();
		ts.rewind();
		IntBuffer ps = Buffers.newDirectIntBuffer(4 * newPolys.keySet().size());
		for (Integer p : newPolys.keySet()) {
			ArrayList<Integer> verts = newPolys.get(p).getAllVertices();
			ps.put(verts.get(0));
			ps.put(verts.get(1));
			ps.put(verts.get(2));
			ps.put(verts.get(3));
		}
		ps.rewind();
		IntBuffer es = Buffers.newDirectIntBuffer(4 * edgeDS.getCreaseSet().toArray().length);
		for (Integer i : edgeDS.getCreaseSet()) {
			EdgeData edgeData = edgeDS.getEdgeData(i);
			int p0 = edgeDS.getVertexData(edgeData.getVertex0()).getNewVertexID();
			int p1 = edgeData.getNewVertexID();
			int p2 = edgeDS.getVertexData(edgeData.getVertex1()).getNewVertexID();
			es.put(p1);
			es.put(p0);
			es.put(p2);
			es.put(p1);
		}
		es.rewind();
		this.mMesh = edgeDS.getMesh();
		Quadmesh m = (Quadmesh) this.mMesh.clone();
		m.setVertexData(vs);
		m.setTexCoordData(ts);
		m.setEdgeData(es);
		m.setPolygonData(ps);
		this.mMesh = (Mesh) m;
		
	}
	
	public Mesh getNewMesh()
	{
		return this.mMesh;
	}
	
}
