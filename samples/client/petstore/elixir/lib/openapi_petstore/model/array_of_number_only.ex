# NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
# https://openapi-generator.tech
# Do not edit the class manually.

defmodule OpenapiPetstore.Model.ArrayOfNumberOnly do
  @moduledoc """
  
  """

  @derive [Poison.Encoder]
  defstruct [
    :"ArrayNumber"
  ]

  @type t :: %__MODULE__{
    :"ArrayNumber" => [float()]
  }
end

defimpl Poison.Decoder, for: OpenapiPetstore.Model.ArrayOfNumberOnly do
  def decode(value, _options) do
    value
  end
end

