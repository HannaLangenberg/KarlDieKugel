#version 430 core
// Vertex Shader based on Basic.vert and BlinnPhongPointTex.vert
// Author: Karsten Lehn
// Version: 7.10.2018

// - Hanna Langenberg
// Version: 30.12.2020

// INPUT
// position
layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec2 vInUV;
layout (location = 2) in vec3 vNormal;
// pvm Matrix
layout (location = 0) uniform mat4 pMatrix;
layout (location = 1) uniform mat4 mvMatrix;
layout (location = 2) uniform mat4 nMatrix;
// light position
layout (location = 3) uniform vec4 lightPosition;

// OUTPUT
out VS_OUT
{
	vec3 N;
	vec3 L;
	vec3 V;
	vec2 UV;
} vs_out;

void main(void)
{
	vec4 P = mvMatrix * vec4(vPosition, 1.0);				// Calculate view-space coordinate
	vs_out.N = (mat4(nMatrix) * vec4(vNormal, 0.0)).xyz;	// Calculate normal in view-space

	vs_out.L = lightPosition.xyz - P.xyz;					// Calculate light vector
	vs_out.V = -P.xyz;										// Calculate view vector

	vs_out.UV = vInUV;  									// Passing UV coordinate

	gl_Position = pMatrix * P;								// Calculate the clip-space position of each vertex
}
