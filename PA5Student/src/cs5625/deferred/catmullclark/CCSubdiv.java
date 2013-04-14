package cs5625.deferred.catmullclark;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Set;

import cs5625.deferred.datastruct.EdgeDS;
import cs5625.deferred.scenegraph.Mesh;
import cs5625.deferred.scenegraph.Trimesh;

public class CCSubdiv {
	
	private Mesh mMesh;
	
	public CCSubdiv(EdgeDS edgeDS)
	{
		// TODO PA5: Fill in this function to perform catmull clark subdivision
		this.mMesh = edgeDS.getMesh();
		FloatBuffer verts = this.mMesh.getVertexData();
		IntBuffer edges = this.mMesh.getEdgeData();
		IntBuffer faces = this.mMesh.getPolygonData();
		
		Set<Integer> vertIds = edgeDS.getVertexIDs();
		Set<Integer> edgeIds = edgeDS.getEdgeIDs();
		Set<Integer> faceIds = edgeDS.getPolygonIDs();
		
		for(Integer vert : vertIds){
			
		}

		for(Integer edge : edgeIds){
			
		}
		
		for(Integer face : faceIds){
				
		}
//		
//		verts.
//		mMesh.
//		
//		// returns all keys
//		getEdgeIDs()
//		getVertexIDs()
//		getPolygonIDs()
//		
//		//returns value for key
//		getEdgeData()
//		getVertexData()
//		getPolygonData()
//		
//		getOtherEdgesOfLeftFace()
//		getOtherEdgesOfRightFace()
//		
//		//subdivide it
//		CCSubdiv loopSubdiv = new CCSubdiv(edgeDS);
//		
//		//create new mesh
//		Trimesh newMesh = (Trimesh)loopSubdiv.getNewMesh();

		
		
	}
	
	public Mesh getNewMesh()
	{
		return this.mMesh;
	}
	
}
