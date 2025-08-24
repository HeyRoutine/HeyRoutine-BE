from pydantic import BaseModel
from typing import List

class TextRequest(BaseModel):
    texts: List[str]

class LabelResponse(BaseModel):
    labels: List[str]
