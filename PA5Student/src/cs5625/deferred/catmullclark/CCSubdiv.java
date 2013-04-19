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

		
		for (Integer polyID : edgeDS.getPolygonIDs()) {
			PolygonData p = edgeDS.getPolygonData(polyID);
			
			Point3f pos;
			Point2f tex = null;
			Point3f p1 = edgeDS.getVertexData(p.getAllVertices().get(0)).mData.getPosition();
			Point3f p2 = edgeDS.getVertexData(p.getAllVertices().get(1)).mData.getPosition();
			Point3f p3 = edgeDS.getVertexData(p.getAllVertices().get(2)).mData.getPosition();
			Point3f p4 = edgeDS.getVertexData(p.getAllVertices().get(3)).mData.getPosition();
			Point3f cvert = new Point3f();
			cvert.add(p1);
			cvert.add(p2);
			cvert.add(p3);
			cvert.add(p4);
			cvert.x = cvert.x / 4;
			cvert.y = cvert.y / 4;
			cvert.z = cvert.z / 4;

			
			
			// Inserting vertex into our TreeMap
			VertexData vertdata = new VertexData(new VertexAttributeData(cvert, tex));
			newVerts.put(newVertID, vertdata); // Put new positions of even
												// vertices in our TreeMap

			// Storing the new vertex in the face it was created in			
			p.setNewFaceVertexID(newVertID++);
			
		}
		// Scan through all edges and create new vertices with correct positions
		for (Integer edgeID : edgeDS.getEdgeIDs()) {
			
			EdgeData edge = edgeDS.getEdgeData(edgeID);
			
			Point3f pos = new Point3f();
			Point2f tex = new Point2f();		
			
	
			if (!(edgeDS.isCreaseEdge(edgeID) || edgeDS
					.getOtherEdgesOfRightFace(edgeID).isEmpty())) {
				// Normal Case
				Point3f temp = edgeDS.getVertexData(edge.getVertex0()).mData.getPosition();
				temp.scale(4);
				pos.add(temp);
				
				Point3f temp2 = edgeDS.getVertexData(edge.getVertex1()).mData.getPosition();
				temp2.scale(4);
				pos.add(temp2);
			
				
				ArrayList<Integer> ps = edge.getPolys();
				ArrayList<Integer> leftVerts = edgeDS.getPolygonData(ps.get(0)).getAllVertices();
				ArrayList<Integer> rightVerts = edgeDS.getPolygonData(ps.get(1)).getAllVertices();
				
				for(Integer v : leftVerts){
					pos.add(edgeDS.getVertexData(v).mData.getPosition());
				}
				
				for(Integer v : rightVerts){
					pos.add(edgeDS.getVertexData(v).mData.getPosition());
				}
				
				pos.scale(1/(2*6 + 4*1));
			
			} else {
				// TODO
				continue;
			}

			
			// Inserting vertex into our TreeMap
			VertexData vertdata = new VertexData(new VertexAttributeData(pos,
					tex));
			newVerts.put(newVertID, vertdata); // Put new positions of even
												// vertices in our TreeMap

			// Storing the new vertex in the edge it was created in
			edge.setVertexIDNew(newVertID++);

		}

		// Scan through all vertices to find new positions of the existing ones
		for (Integer vertID : edgeDS.getVertexIDs()) {
			VertexData vert = edgeDS.getVertexData(vertID);
			int adjacency = vert.getConnectedVertices().size();
			Point3f pos = new Point3f(vert.mData.getPosition());
			Point2f tex = new Point2f(vert.mData.getTexCoord());
		
			
			if (true /* TODO RESUME- vertex is not on creased edge*/) {
//				float scalar1 = (adjacency - 2)/adjacency;
//				Point3f term1 = (Point3f) pos.clone();
//				term1.scale(scalar1);
				
				// based off of 
				// <http://http.developer.nvidia.com/GPUGems2/elementLinks/07_tessellation_02.jpg>
				float scalar = (4 * adjacency * adjacency);
				float centerweight = scalar - (7)*adjacency;
				pos.scale(centerweight);
				
				
				for(Integer eid : vert.getConnectedEdges()){
					EdgeData e = edgeDS.getEdgeData(eid);
					
					// find opposite verts with weight of 1
					for(Integer connected : edgeDS.getOtherEdgesOfRightFace(eid)){
						EdgeData ec = edgeDS.getEdgeData(connected);
						if(ec.getVertex0() == vertID){
							pos.add(edgeDS.getVertexData(ec.getVertex1()).mData.getPosition());
						}else if(ec.getVertex1() == vertID){
							pos.add(edgeDS.getVertexData(ec.getVertex0()).mData.getPosition());
						}
					}
					
					// add verts with weight of 6
					if(e.getVertex0() == vertID){
						Point3f temp = edgeDS.getVertexData(e.getVertex1()).mData.getPosition();
						temp.scale(6);
						pos.add(temp);
					}else if(e.getVertex1() == vertID){
						Point3f temp = edgeDS.getVertexData(e.getVertex0()).mData.getPosition();
						temp.scale(6);
						pos.add(temp);
					}
				}
				
				pos.scale(1/scalar);
			}
	
			VertexData vertdata = new VertexData(new VertexAttributeData(pos,
					tex));
			newVerts.put(newVertID, vertdata); // Put new positions of even
												// vertices in our TreeMap
			vert.setNewVertexID(newVertID++); // Setting the old vertex IDs to
												// the new ones we created
		}


		// Scan through all the pre-existing edges to create new ones
		for (Integer edgeID : edgeDS.getEdgeIDs()) {

			EdgeData edge = edgeDS.getEdgeData(edgeID);
			int newVertex = edge.getNewVertexID();

			int vert0 = edgeDS.getVertexData(edge.getVertex0())
					.getNewVertexID();
			int vert1 = edgeDS.getVertexData(edge.getVertex1())
					.getNewVertexID();

			int edge0n = newEdgeID++;
			int edgen1 = newEdgeID++;
			
			// For each edge put an edge between the 
			//one old vertex and the new one
			newEdges.put(edge0n, new EdgeData(vert0, newVertex)); 
			newEdges.put(edgen1, new EdgeData(newVertex, vert1)); 

			// Store connectivity (in pairs)
			newVerts.get(vert0).addVertexConnectivity(
					new EdgeVertexPair(edge0n, newVertex));
			newVerts.get(newVertex).addVertexConnectivity(
					new EdgeVertexPair(edge0n, vert0));
			newVerts.get(newVertex).addVertexConnectivity(
					new EdgeVertexPair(edgen1, vert1));
			newVerts.get(vert1).addVertexConnectivity(
					new EdgeVertexPair(edgen1, newVertex));
		}

		// Scan through all the pre-existing polygons to create new ones and the
		// remaining edges
		for (Integer polyID : edgeDS.getPolygonIDs()) {
			PolygonData poly = edgeDS.getPolygonData(polyID);
			ArrayList<Integer> allEdges = poly.getAllEdges();

			// Find the new vertices of the face
			int newvert0 = edgeDS.getEdgeData(allEdges.get(0)).getNewVertexID();
			int newvert1 = edgeDS.getEdgeData(allEdges.get(1)).getNewVertexID();
			int newvert2 = edgeDS.getEdgeData(allEdges.get(2)).getNewVertexID();
			int newvert3 = edgeDS.getEdgeData(allEdges.get(3)).getNewVertexID();
			int newvertc = poly.getNewFaceVertexID();

			// Insert the 4 new edges between the new vertices
			int eid1 = newEdgeID++;
			int eid2 = newEdgeID++;
			int eid3 = newEdgeID++;
			int eid4 = newEdgeID++;

			newEdges.put(eid1, new EdgeData(newvert0, newvertc));
			newEdges.put(eid2, new EdgeData(newvert1, newvertc));
			newEdges.put(eid3, new EdgeData(newvert2, newvertc));
			newEdges.put(eid3, new EdgeData(newvert3, newvertc));

			// Get the 4 pre-existing vertices in the quad
			int vert0 = edgeDS.getVertexData(poly.getAllVertices().get(0))
					.getNewVertexID();
			int vert1 = edgeDS.getVertexData(poly.getAllVertices().get(1))
					.getNewVertexID();
			int vert2 = edgeDS.getVertexData(poly.getAllVertices().get(2))
					.getNewVertexID();
			int vert3 = edgeDS.getVertexData(poly.getAllVertices().get(3))
					.getNewVertexID();

			int e1 = newVerts.get(vert0)
					.getEdgeIDforEdgeWithThisVertex(newvert0).getData();
			int e2 = newVerts.get(newvert0)
					.getEdgeIDforEdgeWithThisVertex(vert1).getData();
			int e3 = newVerts.get(vert1)
					.getEdgeIDforEdgeWithThisVertex(newvert1).getData();
			int e4 = newVerts.get(newvert1)
					.getEdgeIDforEdgeWithThisVertex(vert2).getData();
			int e5 = newVerts.get(vert2)
					.getEdgeIDforEdgeWithThisVertex(newvert2).getData();
			int e6 = newVerts.get(newvert2)
					.getEdgeIDforEdgeWithThisVertex(vert3).getData();
			int e7 = newVerts.get(vert3)
					.getEdgeIDforEdgeWithThisVertex(newvert3).getData();
			int e8 = newVerts.get(newvert3)
					.getEdgeIDforEdgeWithThisVertex(vert0).getData();

			// Create the 4 new quads accordingly
			newPolys.put(newPolyID++, new PolygonData(e1,eid1,eid4,e8,vert0,newvert0,newvertc,newvert3));
			newPolys.put(newPolyID++, new PolygonData(e2,e3,eid2,eid1,newvert0,vert1,newvert1,newvertc));
			newPolys.put(newPolyID++, new PolygonData(eid2,e4,e5,eid3,newvertc,newvert1,vert2,newvert2));
			newPolys.put(newPolyID++, new PolygonData(eid4,eid3,e6,e7,newvert3,newvertc,newvert2,vert3));
			

		}

		// Create the Trimesh
		FloatBuffer vs = Buffers.newDirectFloatBuffer(3 * newVerts.keySet()
				.size());
		FloatBuffer ts = Buffers.newDirectFloatBuffer(3 * newVerts.keySet()
				.size());
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
		IntBuffer ps = Buffers.newDirectIntBuffer(3 * newPolys.keySet().size());
		for (Integer p : newPolys.keySet()) {
			ArrayList<Integer> verts = newPolys.get(p).getAllVertices();
			ps.put(verts.get(0));
			ps.put(verts.get(1));
			ps.put(verts.get(2));
		}
		ps.rewind();
		IntBuffer es = Buffers.newDirectIntBuffer(4 * edgeDS.getCreaseSet()
				.toArray().length);
		for (Integer i : edgeDS.getCreaseSet()) {
			EdgeData edgeData = edgeDS.getEdgeData(i);
			int p0 = edgeDS.getVertexData(edgeData.getVertex0())
					.getNewVertexID();
			int p1 = edgeData.getNewVertexID();
			int p2 = edgeDS.getVertexData(edgeData.getVertex1())
					.getNewVertexID();
			es.put(p1);
			es.put(p0);
			es.put(p2);
			es.put(p1);
		}
		es.rewind();
		this.mMesh = edgeDS.getMesh();
		Trimesh m = (Trimesh) this.mMesh.clone();
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
