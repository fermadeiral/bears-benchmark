# NOTE: This class is auto generated by the swagger code generator program.
# https://github.com/swagger-api/swagger-codegen.git
# Do not edit the class manually.

defmodule SwaggerPetstore.Model.User do
  @moduledoc """
  
  """

  @derive [Poison.Encoder]
  defstruct [
    :"id",
    :"username",
    :"firstName",
    :"lastName",
    :"email",
    :"password",
    :"phone",
    :"userStatus"
  ]

  @type t :: %__MODULE__{
    :"id" => integer(),
    :"username" => String.t,
    :"firstName" => String.t,
    :"lastName" => String.t,
    :"email" => String.t,
    :"password" => String.t,
    :"phone" => String.t,
    :"userStatus" => integer()
  }
end

defimpl Poison.Decoder, for: SwaggerPetstore.Model.User do
  def decode(value, _options) do
    value
  end
end

