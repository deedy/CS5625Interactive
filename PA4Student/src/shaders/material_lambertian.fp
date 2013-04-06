/**
 * material_lambertian.fp
 * 
 * Fragment shader shader which writes material information needed for Lambertian shading to
 * the gbuffer.
 * 
 * Written for Cornell CS 5625 (Interactive Computer Graphics).
 * Copyright (c) 2012, Computer Science Department, Cornell University.
 * 
 * @author Asher Dunn (ad488)
 * @date 2012-03-27
 */

/* ID of Lambertian material, so the lighting shader knows what material
 * this pixel is. */
const int LAMBERTIAN_MATERIAL_ID = 2;

/* Material properties passed from the application. */
uniform vec3 DiffuseColor;

/* Textures and flags for whether they exist. */
uniform sampler2D DiffuseTexture;
uniform bool HasDiffuseTexture;

/* Fragment position, normal, and texcoord passed from the vertex shader. */
varying vec3 EyespacePosition;
varying vec3 EyespaceNormal;
varying vec2 TexCoord;

/* Encodes a normalized vector as a vec2. See Renderer.java for more info. */
vec2 encode(vec3 n)
{
	return normalize(n.xy) * sqrt(0.5 * n.z + 0.5);
}

void main()
{
    // Store diffuse color, position, encoded normal, material ID, and all other useful data in the g-buffer.
    vec2 encoded = encode(normalize(EyespaceNormal));

    gl_FragData[0].rgb = DiffuseColor;
    if (HasDiffuseTexture)
    {
        gl_FragData[0].rgb *= texture2D(DiffuseTexture, TexCoord).stp;
    }
    gl_FragData[0].a = encoded.x;

    gl_FragData[1].xyz = EyespacePosition;
    gl_FragData[1].a = encoded.y;

    gl_FragData[2].r = float(LAMBERTIAN_MATERIAL_ID);
    gl_FragData[2].gba = vec3(0.0);

    gl_FragData[3] = vec4(0.0);
}
