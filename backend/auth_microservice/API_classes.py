import uuid

from gen_utils.general_API_classes import GenericReturn, Annotations
from pydantic import BaseModel


class LoginBodyDetails(BaseModel):
    username: str = Annotations.username
    session_id: uuid.UUID = Annotations.session_id


class LoginBodyReturn(GenericReturn):
    detail: LoginBodyDetails


class LogoutBodyDetails(BaseModel):
    username: str = Annotations.username
    session_id: uuid.UUID = Annotations.session_id


class LogoutBodyReturn(GenericReturn):
    detail: LogoutBodyDetails


class ProfileBodyDetails(BaseModel):
    username: str = Annotations.username
    email: str = Annotations.email
    password: str = Annotations.password
    phone_number: str = Annotations.phone_number


class ProfileBodyReturn(GenericReturn):
    message: str = Annotations.message
    detail: ProfileBodyDetails
