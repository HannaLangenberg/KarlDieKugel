#version 430 core
// pretty basic Vertex Shader for Karl

// Author: Hanna Langenberg
// Version: 30.12.2020

// INPUT
// position
layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec3 vNormal;
// pvm Matrix
layout (location = 0) uniform mat4 pMatrix;
layout (location = 1) uniform mat4 mvMatrix;
// surface color
layout (location = 7) uniform vec3 Color;

// OUTPUT
out vec4 vColor;

void main(void) {

	gl_Position = pMatrix * mvMatrix * vec4(vPosition, 1.0);	// clip-space position of each vertex

	float brightness = max(vNormal.y, 0.0);
	vColor = vec4(brightness * Color, 1.0);

}