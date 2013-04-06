/**
 * ssao.fp
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2013, Computer Science Department, Cornell University.
 * 
 * @author Sean Ryan (ser99)
 * @date 2013-03-23
 */

uniform sampler2DRect DiffuseBuffer;
uniform sampler2DRect PositionBuffer;

#define MAX_RAYS 100
uniform int NumRays;
uniform vec3 SampleRays[MAX_RAYS];
uniform float SampleRadius;

uniform mat4 ProjectionMatrix;
uniform vec2 ScreenSize;

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
	// TODO PA4: Implement SSAO. Your output color should be grayscale where white is unobscured and black is fully obscured.
	gl_FragColor = vec4(1.0);
}
