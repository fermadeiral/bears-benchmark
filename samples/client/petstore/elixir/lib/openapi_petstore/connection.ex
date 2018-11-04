# NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
# https://openapi-generator.tech
# Do not edit the class manually.

defmodule OpenapiPetstore.Connection do
  @moduledoc """
  Handle Tesla connections for OpenapiPetstore.
  """

  use Tesla

  # Add any middleware here (authentication)
  plug Tesla.Middleware.BaseUrl, "http://petstore.swagger.io:80/v2"
  plug Tesla.Middleware.Headers, %{"User-Agent" => "Elixir"}
  plug Tesla.Middleware.EncodeJson

  @doc """
  Configure an client connection using Basic authentication.

  ## Parameters

  - username (String): Username used for authentication
  - password (String): Password used for authentication

  # Returns

  Tesla.Env.client
  """
  @spec new(String.t, String.t) :: Tesla.Env.client
  def new(username, password) do
    Tesla.build_client([
      {Tesla.Middleware.BasicAuth, %{username: username, password: password}}
    ])
  end
  @scopes [
    "write:pets", # modify pets in your account
    "read:pets" # read your pets
  ]

  @doc """
  Configure a client connection using a provided OAuth2 token as a Bearer token

  ## Parameters

  - token (String): Bearer token

  ## Returns

  Tesla.Env.client
  """
  @spec new(String.t) :: Tesla.Env.client
  def new(token) when is_binary(token) do
    Tesla.build_client([
      {Tesla.Middleware.Headers,  %{"Authorization" => "Bearer #{token}"}}
    ])
  end

  @doc """
  Configure a client connection using a function which yields a Bearer token.

  ## Parameters

  - token_fetcher (function arity of 1): Callback which provides an OAuth2 token
    given a list of scopes

  ## Returns

  Tesla.Env.client
  """
  @spec new(((list(String.t)) -> String.t)) :: Tesla.Env.client
  def new(token_fetcher) when is_function(token_fetcher) do
    token_fetcher.(@scopes)
    |> new
  end
  @doc """
  Configure an authless client connection

  # Returns

  Tesla.Env.client
  """
  @spec new() :: Tesla.Env.client
  def new do
    Tesla.build_client([])
  end
end
