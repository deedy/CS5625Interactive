/**
 * visualize.fp
 * 
 * Fragment shader for the data visualization post-processing stage.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2013, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488), Sean Ryan (ser99)
 * @date 2013-01-31
 */

/* We need the anisotropic ward ID so that we can detect tangent data. */
const int ANISOTROPIC_WARD_MATERIAL_ID = 6;

/* Samplers for each texture of the GBuffer. */
uniform sampler2DRect DiffuseBuffer;
uniform sampler2DRect PositionBuffer;
uniform sampler2DRect MaterialParams1Buffer;
uniform sampler2DRect MaterialParams2Buffer;

/* Determines what kind of data we are rendering. */
const int NORMALS_MODE = 0;
const int TANGENTS_MODE = 1;
const int BITANGENTS_MODE = 2;
uniform int VisMode;

/* Decodes a vec2 into a normalized vector See Renderer.java for more info. */
vec3 decode(vec2 v)
{
	vec3 n;
	n.z = 2.0 * dot(v.xy, v.xy) - 1.0;
	n.xy = normalize(v.xy) * sqrt(1.0 - n.z*n.z);
	return n;
}

void main()
{
	vec3 normal = decode(vec2(texture2DRect(DiffuseBuffer,  gl_FragCoord.xy).a,
	                          texture2DRect(PositionBuffer, gl_FragCoord.xy).a));
	vec4 materialParams1 = texture2DRect(MaterialParams1Buffer, gl_FragCoord.xy);
	vec4 materialParams2 = texture2DRect(MaterialParams2Buffer, gl_FragCoord.xy);
	int materialID = int(materialParams1.x);
	
	// TODO PA4 Prereq (Optional): Paste in your vizualization shader if you feel like it.
	gl_FragColor = vec4(0.0);
}
