package com.clerami.universe.data.remote.response

import com.google.gson.annotations.SerializedName

data class Response(

	@field:SerializedName("guestHandler")
	val guestHandler: GuestHandler? = null,

	@field:SerializedName("oauthCallbackHandler")
	val oauthCallbackHandler: OauthCallbackHandler? = null,

	@field:SerializedName("signUpHandler")
	val signUpHandler: SignUpHandler? = null,

	@field:SerializedName("loginHandler")
	val loginHandler: LoginHandler? = null,

	@field:SerializedName("oauthLoginHandler")
	val oauthLoginHandler: OauthLoginHandler? = null
)

data class ErrorHandling(

	@field:SerializedName("400")
	val jsonMember400: String? = null,

	@field:SerializedName("500")
	val jsonMember500: String? = null,

	@field:SerializedName("401")
	val jsonMember401: String? = null
)

data class OauthLoginHandler(

	@field:SerializedName("output")
	val output: Output? = null,

	@field:SerializedName("input")
	val input: Input? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("errorHandling")
	val errorHandling: ErrorHandling? = null
)

data class Input(

	@field:SerializedName("provider")
	val provider: String? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("usernameOrEmail")
	val usernameOrEmail: String? = null,

	@field:SerializedName("confirmPassword")
	val confirmPassword: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)

data class Output(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("user")
	val user: User? = null,

	@field:SerializedName("username")
	val username: String? = null
)

data class GuestHandler(

	@field:SerializedName("output")
	val output: Output? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("errorHandling")
	val errorHandling: ErrorHandling? = null
)

data class SignUpHandler(

	@field:SerializedName("output")
	val output: Output? = null,

	@field:SerializedName("input")
	val input: Input? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("errorHandling")
	val errorHandling: ErrorHandling? = null
)

data class OauthCallbackHandler(

	@field:SerializedName("output")
	val output: Output? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("errorHandling")
	val errorHandling: ErrorHandling? = null
)

data class User(

	@field:SerializedName("uid")
	val uid: String? = null,

	@field:SerializedName("displayName")
	val displayName: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)

data class LoginHandler(

	@field:SerializedName("output")
	val output: Output? = null,

	@field:SerializedName("input")
	val input: Input? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("errorHandling")
	val errorHandling: ErrorHandling? = null
)
