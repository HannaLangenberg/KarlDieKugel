#version 430 core
// pretty basic Fragment Shader for Karl

// Author: Hanna Langenberg
// Version: 30.12.2020

// INPUT
in vec4 vColor;

// OUTPUT
out vec4 FragColor;

void main (void)
{
	FragColor = vColor;
}