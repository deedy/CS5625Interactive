package cs5625.deferred.misc;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Point3f;

import com.jogamp.common.nio.Buffers;

import cs5625.deferred.scenegraph.Quadmesh;

public class QuadmeshMaker {

	private Quadmesh mMesh;
	private int numVertices, numTris, numCreases;
	
	private FloatBuffer positionBuffer;
	private IntBuffer indexBuffer;

	private IntBuffer edgeBuffer;
	
	private int quadsAdded;
	private int creasesAdded;
		
	public QuadmeshMaker(int numVertices_, int numTris_, int numCreases_)
	{
		this.numTris = numTris_;
		this.numVertices = numVertices_;
		this.numCreases = numCreases_;
		
		this.positionBuffer = Buffers.newDirectFloatBuffer(3 * this.numVertices);
		this.indexBuffer = Buffers.newDirectIntBuffer(4 * this.numTris);
		
		this.edgeBuffer = Buffers.newDirectIntBuffer(2 * this.numCreases);
		
		this.quadsAdded = 0;
		this.creasesAdded = 0;
		
//		this.newVertexID = 0;
//		this.mIndexRemap = new TreeMap<Integer,Integer>();
		
		this.mMesh = new Quadmesh();
	}
	
//	private int getNewVertexIDAferRemapping()
//	{
//		int retval = this.newVertexID;
//		this.newVertexID = this.newVertexID + 1;
//		return retval;
//	}
	
	private int getNewCreaseID()
	{
		int retval = this.creasesAdded;
		this.creasesAdded = this.creasesAdded + 1;
		return retval;
	}
	
//	private int remapVertexID(int oldVertexID)
//	{
//		Integer newVertexID = this.mIndexRemap.get(oldVertexID);
//		if(newVertexID == null)
//		{	
//			//not in remap LUT
//			int retval = this.getNewVertexIDAferRemapping();
//			//so get new
//			this.mIndexRemap.put(oldVertexID,retval);
//			//store new
//			return retval;
//			//return that
//		}
//		else
//			//return what you found
//			return newVertexID;
//	}
	
	public void addQuad(Point3f v0, Point3f v1, Point3f v2, Point3f v3, int i0_, int i1_, int i2_, int i3_)
	{
		//FIXME: needs to be updated to a quad
//		int i0 = this.remapVertexID(i0_);
//		int i1 = this.remapVertexID(i1_);
//		int i2 = this.remapVertexID(i2_);
		
		final int i0 = i0_;
		final int i1 = i1_;
		final int i2 = i2_;
		final int i3 = i3_;
		
		
		//add v0;
		this.positionBuffer.put(3*i0 + 0, v0.x);
		this.positionBuffer.put(3*i0 + 1, v0.y);
		this.positionBuffer.put(3*i0 + 2, v0.z);
		
		//add v1;
		this.positionBuffer.put(3*i1 + 0, v1.x);
		this.positionBuffer.put(3*i1 + 1, v1.y);
		this.positionBuffer.put(3*i1 + 2, v1.z);
		
		//add v2;
		this.positionBuffer.put(3*i2 + 0, v2.x);
		this.positionBuffer.put(3*i2 + 1, v2.y);
		this.positionBuffer.put(3*i2 + 2, v2.z);

		//add v3;
		this.positionBuffer.put(3*i3 + 0, v3.x);
		this.positionBuffer.put(3*i3 + 1, v3.y);
		this.positionBuffer.put(3*i3 + 2, v3.z);
		
		this.indexBuffer.put(4 * this.quadsAdded + 0, i0);
		this.indexBuffer.put(4 * this.quadsAdded + 1, i1);
		this.indexBuffer.put(4 * this.quadsAdded + 2, i2);
		this.indexBuffer.put(4 * this.quadsAdded + 3, i3);
		
		this.quadsAdded = this.quadsAdded + 1;
	}
	
	public void addCrease(int v0, int v1)
	{
		int creaseID = this.getNewCreaseID();
		this.edgeBuffer.put(2 * creaseID + 0, v0);
		this.edgeBuffer.put(2 * creaseID + 1, v1);
	}
	
	public Quadmesh getMesh()
	{
		this.indexBuffer.rewind();
		this.positionBuffer.rewind();
		this.mMesh.setVertexData(this.positionBuffer);
		this.mMesh.setPolygonData(this.indexBuffer);
		
		FloatBuffer texCoordData = Buffers.newDirectFloatBuffer(2 * numVertices);
		FloatBuffer normalData = Buffers.newDirectFloatBuffer(3 * numVertices);
		
		this.mMesh.setNormalData(normalData);
		this.mMesh.setTexCoordData(texCoordData);		

		this.mMesh.setEdgeData(edgeBuffer);
		
		return this.mMesh;
	}
}
