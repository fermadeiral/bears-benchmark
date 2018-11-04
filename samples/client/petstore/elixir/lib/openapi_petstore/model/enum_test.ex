# NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
# https://openapi-generator.tech
# Do not edit the class manually.

defmodule OpenapiPetstore.Model.EnumTest do
  @moduledoc """
  
  """

  @derive [Poison.Encoder]
  defstruct [
    :"enum_string",
    :"enum_string_required",
    :"enum_integer",
    :"enum_number",
    :"outerEnum"
  ]

  @type t :: %__MODULE__{
    :"enum_string" => String.t,
    :"enum_string_required" => String.t,
    :"enum_integer" => integer(),
    :"enum_number" => float(),
    :"outerEnum" => OuterEnum
  }
end

defimpl Poison.Decoder, for: OpenapiPetstore.Model.EnumTest do
  import OpenapiPetstore.Deserializer
  def decode(value, options) do
    value
    |> deserialize(:"outerEnum", :struct, OpenapiPetstore.Model.OuterEnum, options)
  end
end

