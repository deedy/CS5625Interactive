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
	vec4 origin = texture2DRect(PositionBuffer, vec2(0.0,0.0));

	
	vec3 normal = vec3(0.0,1.0,0.0);
	vec3 rvec = vec3(0.0, 1.0, 0.0);
	vec3 tangent = normalize(rvec - normal * dot(rvec, normal));
   	vec3 bitangent = cross(normal, tangent);
   	mat3 tbn = mat3(tangent, bitangent, normal);
	float ssao = 0.0;
	int i;
	for (i = 0; i < NumRays;i++) {
		vec3 sample = tbn * SampleRays[i];
		sample = sample * SampleRadius;
		vec4 offset = vec4(sample, 1.0);
		offset = ProjectionMatrix * offset + origin;
		offset.xy /= offset.w;
		offset.xy = offset.xy * 0.5 + 0.5;
		float sampleDepth = texture2DRect(PositionBuffer, offset.xy).r;
		float rangeCheck= abs(origin.z - sampleDepth) < SampleRadius ? 1.0 : 0.0;
		ssao += (sampleDepth <= sample.z ? 1.0 : 0.0) * rangeCheck;
	}
	
	gl_FragColor = vec4(ssao,ssao,ssao,1.0);
}
