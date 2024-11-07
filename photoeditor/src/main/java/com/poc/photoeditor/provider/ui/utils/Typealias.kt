package com.poc.photoeditor.provider.ui.utils

import android.view.LayoutInflater
import android.view.ViewGroup

typealias InflateFragmentAlias<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

typealias InflateActivityAlias<T> = (LayoutInflater) -> T

typealias InflateAlias<T> = (LayoutInflater, ViewGroup?, Boolean) -> T